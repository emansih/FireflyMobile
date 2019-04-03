package xyz.hisname.fireflyiii.repository.account

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.api.AccountsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.models.accounts.AccountSuccessModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.network.NetworkErrors
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import xyz.hisname.fireflyiii.workers.account.AccountWorker
import xyz.hisname.fireflyiii.workers.account.DeleteAccountWorker
import java.text.DecimalFormat

class AccountsViewModel(application: Application): BaseViewModel(application){

    val repository: AccountRepository
    var accountData: MutableList<AccountData>? = null
    private val accountsService by lazy { genericService()?.create(AccountsService::class.java) }
    val emptyAccount: MutableLiveData<Boolean> = MutableLiveData()

    init {
        val accountDao = AppDatabase.getInstance(application).accountDataDao()
        repository = AccountRepository(accountDao)
    }


    fun getAllAccounts(): LiveData<MutableList<AccountData>> {
        loadRemoteData("all")
        return repository.allAccounts
    }

    fun getAllAccountWithNetworthAndCurrency(currencyCode: String): LiveData<String>{
        isLoading.value = true
        val accountValue: MutableLiveData<String> = MutableLiveData()
        val df = DecimalFormat("#.##")
        var currentBalance = 0.toDouble()
        accountsService?.getPaginatedAccountType("all", 1)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val networkData = response.body()
                if (networkData != null) {
                    if(networkData.meta.pagination.current_page == networkData.meta.pagination.total_pages) {
                        networkData.data.forEachIndexed { _, accountData ->
                            scope.launch(Dispatchers.IO) { repository.insertAccount(accountData) }
                        }
                    } else {
                        networkData.data.forEachIndexed { _, accountData ->
                            scope.launch(Dispatchers.IO) { repository.insertAccount(accountData) }
                        }
                        for (pagination in 2..networkData.meta.pagination.total_pages) {
                            accountsService?.getPaginatedAccountType("all", pagination)?.enqueue(retrofitCallback({ respond ->
                                respond.body()?.data?.forEachIndexed { _, accountPagination ->
                                    scope.launch(Dispatchers.IO) { repository.insertAccount(accountPagination) }
                                }
                            }))
                        }
                    }
                    scope.launch(Dispatchers.IO){
                        accountData = repository.retrieveAccountWithCurrencyCodeAndNetworth(currencyCode)
                    }.invokeOnCompletion {
                        accountData?.forEachIndexed { _, accountData ->
                            currentBalance += accountData.accountAttributes?.current_balance ?: 0.toDouble()
                        }
                        accountValue.postValue(df.format(currentBalance).toString())
                    }
                    isLoading.value = false
                }
            } else {
                val responseError = response.errorBody()
                if (responseError != null) {
                    val errorBody = String(responseError.bytes())
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    apiResponse.postValue(gson.message)
                }
                scope.launch(Dispatchers.IO){
                    accountData = repository.retrieveAccountWithCurrencyCodeAndNetworth(currencyCode)
                }.invokeOnCompletion {
                    accountData?.forEachIndexed { _, accountData ->
                        currentBalance += accountData.accountAttributes?.current_balance ?: 0.toDouble()
                    }
                    accountValue.postValue(df.format(currentBalance).toString())
                }
                isLoading.value = false
            }
        })
        { throwable ->
            scope.launch(Dispatchers.IO){
                accountData = repository.retrieveAccountWithCurrencyCodeAndNetworth(currencyCode)
            }.invokeOnCompletion {
                accountData?.forEachIndexed { _, accountData ->
                    currentBalance += accountData.accountAttributes?.current_balance ?: 0.toDouble()
                }
                accountValue.postValue(currentBalance.toString())
            }
            isLoading.value = false
            apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage))
        })
        return accountValue
    }

    fun getAssetAccounts(): LiveData<MutableList<AccountData>> {
        loadRemoteData("asset")
        return repository.assetAccount
    }

    fun getExpenseAccounts(): LiveData<MutableList<AccountData>> {
        loadRemoteData("expense")
        return repository.expenseAccount
    }

    fun getRevenueAccounts(): LiveData<MutableList<AccountData>> {
        loadRemoteData("revenue")
        return repository.revenueAccount
    }

    fun getLiabilityAccounts(): LiveData<MutableList<AccountData>> {
        loadRemoteData("liability")
        return repository.liabilityAccount
    }

    fun getAccountById(id: Long): LiveData<MutableList<AccountData>>{
        val accountData: MutableLiveData<MutableList<AccountData>> = MutableLiveData()
        var data: MutableList<AccountData> = arrayListOf()
        scope.async(Dispatchers.IO) {
            data = repository.retrieveAccountById(id)
        }.invokeOnCompletion {
            accountData.postValue(data)
        }
        return accountData
    }

    fun deleteAccountById(accountId: Long): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        isLoading.value = true
        accountsService?.deleteAccountById(accountId)?.enqueue(retrofitCallback({ response ->
            if (response.code() == 204 || response.code() == 200) {
                scope.async(Dispatchers.IO) {
                    repository.deleteAccountById(accountId)
                }.invokeOnCompletion {
                    isDeleted.postValue(true)
                }
            } else {
                isDeleted.postValue(false)
                deleteAccount(accountId)
            }
        })
        { throwable ->
            isDeleted.postValue(false)
            deleteAccount(accountId)
        })
        isLoading.value = false
        return isDeleted
    }

    fun getAccountByName(accountName: String): LiveData<MutableList<AccountData>>{
        val accountData: MutableLiveData<MutableList<AccountData>> = MutableLiveData()
        var data: MutableList<AccountData> = arrayListOf()
        scope.async(Dispatchers.IO) {
            data = repository.retrieveAccountByName(accountName)
        }.invokeOnCompletion {
            accountData.postValue(data)
        }
        return accountData
    }

    fun addAccounts(accountName: String, accountType: String,
                    currencyCode: String?, iban: String?, bic: String?, accountNumber: String?,
                    openingBalance: String?, openingBalanceDate: String?, accountRole: String?,
                    virtualBalance: String?, includeInNetWorth: Boolean, notes: String?, liabilityType: String?,
                    liabilityAmount: String?, liabilityStartDate: String?, interest: String?, interestPeriod: String?): LiveData<ApiResponses<AccountSuccessModel>>{
        isLoading.value = true
        val apiResponse: MediatorLiveData<ApiResponses<AccountSuccessModel>> =  MediatorLiveData()
        val apiLiveData: MutableLiveData<ApiResponses<AccountSuccessModel>> = MutableLiveData()
        accountsService?.addAccount(accountName, accountType, currencyCode, iban, bic, accountNumber,
                openingBalance, openingBalanceDate, accountRole, virtualBalance, includeInNetWorth,
                notes, liabilityType, liabilityAmount, liabilityStartDate, interest, interestPeriod)?.enqueue(retrofitCallback({ response ->
            var errorMessage = ""
            val responseErrorBody = response.errorBody()
            if (responseErrorBody != null) {
                errorMessage = String(responseErrorBody.bytes())
                val gson = Gson().fromJson(errorMessage, ErrorModel::class.java)
                errorMessage = when {
                    gson.errors.name != null -> gson.errors.name[0]
                    gson.errors.account_number != null -> gson.errors.account_number[0]
                    gson.errors.interest != null -> gson.errors.interest[0]
                    gson.errors.liabilityStartDate != null -> gson.errors.liabilityStartDate[0]
                    gson.errors.currency_code != null -> gson.errors.currency_code[0]
                    gson.errors.iban != null -> gson.errors.iban[0]
                    gson.errors.bic != null -> gson.errors.bic[0]
                    gson.errors.opening_balance != null -> gson.errors.opening_balance[0]
                    gson.errors.opening_balance_date != null -> gson.errors.opening_balance_date[0]
                    gson.errors.interest_period != null -> gson.errors.interest_period[0]
                    gson.errors.liability_amount != null -> gson.errors.liability_amount[0]
                    else -> "Error occurred while saving Account"
                }
            }
            val networkResponse = response.body()?.data
            if (networkResponse != null) {
                scope.async(Dispatchers.IO) {
                    repository.insertAccount(networkResponse)
                }.invokeOnCompletion {
                    apiLiveData.postValue(ApiResponses(response.body()))
                }
            } else {
                apiLiveData.postValue(ApiResponses(errorMessage))
            }

        })
        { throwable ->
            apiLiveData.postValue(ApiResponses(throwable))
            AccountWorker.initWorker(accountName, accountType, currencyCode, iban, bic, accountNumber,
                    openingBalance, openingBalanceDate, accountRole, virtualBalance, includeInNetWorth,
                    notes, liabilityType, liabilityAmount, liabilityStartDate, interest, interestPeriod)
        })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        isLoading.value = false
        return apiResponse
    }

    fun updateAccount(accountId: Long, accountName: String, accountType: String,
                      currencyCode: String?, iban: String?, bic: String?, accountNumber: String?,
                      openingBalance: String?, openingBalanceDate: String?, accountRole: String?,
                      virtualBalance: String?, includeInNetWorth: Boolean, notes: String?, liabilityType: String?,
                      liabilityAmount: String?, liabilityStartDate: String?, interest: String?,
                      interestPeriod: String?): LiveData<ApiResponses<AccountSuccessModel>>{
        isLoading.value = true
        val apiResponse: MediatorLiveData<ApiResponses<AccountSuccessModel>> =  MediatorLiveData()
        val apiLiveData: MutableLiveData<ApiResponses<AccountSuccessModel>> = MutableLiveData()
        accountsService?.updateAccount(accountId, accountName, accountType, currencyCode, iban, bic, accountNumber,
                openingBalance, openingBalanceDate, accountRole, virtualBalance, includeInNetWorth,
                notes, liabilityType, liabilityAmount, liabilityStartDate, interest, interestPeriod)?.enqueue(retrofitCallback({ response ->
            var errorMessage = ""
            val responseErrorBody = response.errorBody()
            if (responseErrorBody != null) {
                errorMessage = String(responseErrorBody.bytes())
                val gson = Gson().fromJson(errorMessage, ErrorModel::class.java)
                errorMessage = when {
                    gson.errors.name != null -> gson.errors.name[0]
                    gson.errors.account_number != null -> gson.errors.account_number[0]
                    gson.errors.interest != null -> gson.errors.interest[0]
                    gson.errors.liabilityStartDate != null -> gson.errors.liabilityStartDate[0]
                    gson.errors.currency_code != null -> gson.errors.currency_code[0]
                    gson.errors.iban != null -> gson.errors.iban[0]
                    gson.errors.bic != null -> gson.errors.bic[0]
                    gson.errors.opening_balance != null -> gson.errors.opening_balance[0]
                    gson.errors.opening_balance_date != null -> gson.errors.opening_balance_date[0]
                    gson.errors.interest_period != null -> gson.errors.interest_period[0]
                    gson.errors.liability_amount != null -> gson.errors.liability_amount[0]
                    else -> "Error occurred while updating Account"
                }
            }
            val networkResponse = response.body()?.data
            if (networkResponse != null) {
                scope.async(Dispatchers.IO) {
                    repository.insertAccount(networkResponse)
                }.invokeOnCompletion {
                    apiLiveData.postValue(ApiResponses(response.body()))
                }
            } else {
                apiLiveData.postValue(ApiResponses(errorMessage))
            }

        })
        { throwable -> apiLiveData.postValue(ApiResponses(throwable)) })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        isLoading.value = false
        return apiResponse
    }

    private fun deleteAccount(id: Long) = DeleteAccountWorker.deleteWorker(id)

    private fun loadRemoteData(source: String){
        isLoading.value = true
        apiResponse.value = null
        val totalAccountList = arrayListOf<AccountData>()
        genericService()?.create(AccountsService::class.java)?.getPaginatedAccountType(source, 1)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val networkData = response.body()
                if (networkData != null) {
                    totalAccountList.addAll(networkData.data)
                    if(networkData.meta.pagination.current_page > networkData.meta.pagination.total_pages) {
                        for (pagination in 2..networkData.meta.pagination.total_pages) {
                            genericService()?.create(AccountsService::class.java)?.getPaginatedAccountType(source, pagination)?.enqueue(retrofitCallback({ respond ->
                                respond.body()?.data?.forEachIndexed { _, accountPagination ->
                                    totalAccountList.add(accountPagination)
                                }
                            }))
                        }
                    }
                    scope.launch(Dispatchers.IO){
                        repository.deleteAccountByType(source)
                    }.invokeOnCompletion {
                        scope.launch(Dispatchers.IO) {
                            totalAccountList.forEachIndexed { _, accountData ->
                                repository.insertAccount(accountData)
                            }
                        }
                    }
                    isLoading.value = false
                }
            } else {
                val responseError = response.errorBody()
                if (responseError != null) {
                    val errorBody = String(responseError.bytes())
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    apiResponse.postValue(gson.message)
                }
                isLoading.value = false
            }
        })
        { throwable ->
            isLoading.value = false
            apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage))
        })
    }

}