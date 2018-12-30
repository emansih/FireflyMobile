package xyz.hisname.fireflyiii.repository.account

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
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
import xyz.hisname.fireflyiii.workers.account.DeleteAccountWorker
import kotlin.math.round

class AccountsViewModel(application: Application): BaseViewModel(application){

    private var asset = 0.toDouble()
    private var assetValue: MutableLiveData<String> = MutableLiveData()
    private var cash = 0.toDouble()
    private var cashValue: MutableLiveData<String> = MutableLiveData()
    private var expense = 0.toDouble()
    private var expenseValue: MutableLiveData<String> = MutableLiveData()
    val repository: AccountRepository
    var accountData: MutableList<AccountData>? = null
    private val accountsService by lazy { genericService()?.create(AccountsService::class.java) }
    val emptyAccount: MutableLiveData<Boolean> = MutableLiveData()

    init {
        val accountDao = AppDatabase.getInstance(application).accountDataDao()
        repository = AccountRepository(accountDao)
    }


    fun getTotalAssetAccount(currencyCode: String): LiveData<String> {
        loadRemoteData("asset")
        asset = 0.toDouble()
        scope.async(Dispatchers.IO) {
            accountData = repository.retrieveAccountByTypeWithCurrency("asset", currencyCode)
        }.invokeOnCompletion {
            isLoading.postValue(false)
            if (accountData.isNullOrEmpty()) {
                asset = 0.toDouble()
            } else {
                accountData?.forEachIndexed { _, accountData ->
                    asset += accountData.accountAttributes?.current_balance!!
                }
            }
            asset = round(asset)
            assetValue.postValue(asset.toString())
        }
        return assetValue
    }

    fun getTotalCashAccount(currencyCode: String): LiveData<String>{
        loadRemoteData("cash")
        cash = 0.toDouble()
        scope.async(Dispatchers.IO) {
            accountData = repository.retrieveAccountByTypeWithCurrency("cash", currencyCode)
        }.invokeOnCompletion {
            isLoading.postValue(false)
            if (accountData.isNullOrEmpty()) {
                cash = 0.toDouble()
            } else {
                accountData?.forEachIndexed { _, accountData ->
                    cash += accountData.accountAttributes?.current_balance!!
                }
            }
            cash = round(cash)
            cashValue.postValue(cash.toString())
        }
        return cashValue
    }

    fun getTotalExpenseAccount(currencyCode: String): LiveData<String>{
        loadRemoteData("expense")
        expense = 0.toDouble()
        scope.async(Dispatchers.IO) {
            accountData = repository.retrieveAccountByTypeWithCurrency("expense", currencyCode)
        }.invokeOnCompletion {
            isLoading.postValue(false)
            if (accountData.isNullOrEmpty()) {
                expense = 0.toDouble()
            } else {
                accountData?.forEachIndexed { _, accountData ->
                    expense += accountData.accountAttributes?.current_balance!!
                }
            }
            expense = round(expense)
            expenseValue.postValue(expense.toString())
        }
        return expenseValue
    }

    fun getAllAccounts(): LiveData<MutableList<AccountData>> {
        loadRemoteData("all")
        return repository.allAccounts
    }

    fun getAssetAccounts(): LiveData<MutableList<AccountData>> {
        isLoading.value = true
        accountsService?.getAccountType("asset")?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val networkData = response.body()
                if (networkData != null) {
                    for (pagination in 1..networkData.meta.pagination.total_pages) {
                        accountsService!!.getPaginatedAccountType("asset", pagination).enqueue(retrofitCallback({ respond ->
                            respond.body()?.data?.forEachIndexed { _, accountPagination ->
                                scope.launch(Dispatchers.IO) { repository.insertAccount(accountPagination) }
                            }
                        }))
                    }
                    if(networkData.data.isEmpty()){
                        emptyAccount.value = true
                    }
                }
            } else {
                val responseError = response.errorBody()
                if (responseError != null) {
                    val errorBody = String(responseError.bytes())
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    apiResponse.postValue(gson.message)
                }
            }
        })
        { throwable -> apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage)) })
        isLoading.value = false
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
                    currencyCode: String, includeNetWorth: Int, accountRole: String?,
                    ccType: String?, ccMonthlyPaymentDate: String?, liabilityType: String?,
                    liabilityAmount: String?,liabilityStartDate: String?, interest: String?,
                    interestPeriod: String?,accountNumber: String?): LiveData<ApiResponses<AccountSuccessModel>>{
        isLoading.value = true
        val apiResponse: MediatorLiveData<ApiResponses<AccountSuccessModel>> =  MediatorLiveData()
        val apiLiveData: MutableLiveData<ApiResponses<AccountSuccessModel>> = MutableLiveData()
        accountsService?.addAccount(accountName, accountType, currencyCode,1, includeNetWorth,
                accountRole, ccType, ccMonthlyPaymentDate, liabilityType, liabilityAmount, liabilityStartDate,
                interest, interestPeriod, accountNumber)?.enqueue(retrofitCallback({ response ->
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
        { throwable -> apiLiveData.postValue(ApiResponses(throwable)) })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        isLoading.value = false
        return apiResponse
    }

    private fun deleteAccount(id: Long){
        val accountTag =
                WorkManager.getInstance().getWorkInfosByTag("delete_account_$id").get()
        if(accountTag == null || accountTag.size == 0) {
            val accountData = Data.Builder()
                    .putLong("id", id)
                    .build()
            val deleteAccountWork = OneTimeWorkRequest.Builder(DeleteAccountWorker::class.java)
                    .setInputData(accountData)
                    .addTag("delete_account_$id")
                    .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build()
            WorkManager.getInstance().enqueue(deleteAccountWork)
        }
    }

    private fun loadRemoteData(source: String){
        isLoading.value = true
        accountsService?.getAccountType(source)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val networkData = response.body()
                if (networkData != null) {
                    for (pagination in 1..networkData.meta.pagination.total_pages) {
                        accountsService!!.getPaginatedAccountType(source, pagination).enqueue(retrofitCallback({ respond ->
                            respond.body()?.data?.forEachIndexed { _, accountPagination ->
                                scope.launch(Dispatchers.IO) { repository.insertAccount(accountPagination) }
                            }
                        }))
                    }
                }
            } else {
                val responseError = response.errorBody()
                if (responseError != null) {
                    val errorBody = String(responseError.bytes())
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    apiResponse.postValue(gson.message)
                }
            }
        })
        { throwable -> apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage)) })
        isLoading.value = false
    }

}