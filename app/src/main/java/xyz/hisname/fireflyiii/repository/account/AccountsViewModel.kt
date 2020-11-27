package xyz.hisname.fireflyiii.repository.account

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.models.accounts.AccountSuccessModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import xyz.hisname.fireflyiii.workers.account.AccountWorker
import xyz.hisname.fireflyiii.workers.account.DeleteAccountWorker


class AccountsViewModel(application: Application): BaseViewModel(application){

    val repository: AccountRepository
    private val accountsService by lazy { genericService()?.create(AccountsService::class.java) }

    init {
        val accountDao = AppDatabase.getInstance(application).accountDataDao()
        repository = AccountRepository(accountDao, accountsService)
    }

    fun getAccountByType(accountType: String): LiveData<MutableList<AccountData>> {
        val accountData: MutableLiveData<MutableList<AccountData>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAccountByType(accountType).collectLatest {
                accountData.postValue(it)
            }
        }
        return accountData
    }

    fun getAccountNameByType(accountType: String): LiveData<MutableList<String>>{
        val accountData: MutableLiveData<MutableList<String>> = MutableLiveData()
        val data: MutableList<String> = arrayListOf()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAccountByType(accountType).collectLatest { accountInfo ->
                accountInfo.forEach { accountData ->
                    data.add(accountData.accountAttributes?.name ?: "")
                }
                accountData.postValue(data)
            }
        }
        return accountData
    }

    fun getAccountById(id: Long): LiveData<MutableList<AccountData>>{
        val accountData: MutableLiveData<MutableList<AccountData>> = MutableLiveData()
        var data: MutableList<AccountData> = arrayListOf()
        viewModelScope.launch(Dispatchers.IO) {
            data = repository.retrieveAccountById(id)
        }.invokeOnCompletion {
            accountData.postValue(data)
        }
        return accountData
    }

    fun deleteAccountById(accountId: Long): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        var isItDeleted = 0
        isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            isItDeleted = repository.deleteAccountById(accountId)
        }.invokeOnCompletion {
            when (isItDeleted) {
                HttpConstants.FAILED -> {
                    isDeleted.postValue(false)
                    DeleteAccountWorker.initPeriodicWorker(accountId, getApplication())
                }
                HttpConstants.UNAUTHORISED -> {
                    isDeleted.postValue(false)
                }
                HttpConstants.NO_CONTENT_SUCCESS -> {
                    isDeleted.postValue(true)
                }
            }
            isLoading.postValue(false)

        }
        return isDeleted
    }

    fun getAccountByName(accountName: String): LiveData<MutableList<AccountData>>{
        val accountData: MutableLiveData<MutableList<AccountData>> = MutableLiveData()
        var data: MutableList<AccountData> = arrayListOf()
        viewModelScope.launch(Dispatchers.IO) {
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
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorMessage)
                errorMessage = when {
                    moshi?.errors?.name != null -> moshi.errors.name[0]
                    moshi?.errors?.account_number != null -> moshi.errors.account_number[0]
                    moshi?.errors?.interest != null -> moshi.errors.interest[0]
                    moshi?.errors?.liabilityStartDate != null -> moshi.errors.liabilityStartDate[0]
                    moshi?.errors?.currency_code != null -> moshi.errors.currency_code[0]
                    moshi?.errors?.iban != null -> moshi.errors.iban[0]
                    moshi?.errors?.bic != null -> moshi.errors.bic[0]
                    moshi?.errors?.opening_balance != null -> moshi.errors.opening_balance[0]
                    moshi?.errors?.opening_balance_date != null -> moshi.errors.opening_balance_date[0]
                    moshi?.errors?.interest_period != null -> moshi.errors.interest_period[0]
                    moshi?.errors?.liability_amount != null -> moshi.errors.liability_amount[0]
                    moshi?.errors?.exception != null -> moshi.errors.exception[0]
                    else -> "Error occurred while saving Account"
                }
            }
            val networkResponse = response.body()?.data
            if (networkResponse != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.insertAccount(networkResponse)
                }.invokeOnCompletion {
                    apiLiveData.postValue(ApiResponses(response.body()))
                }
            } else {
                apiLiveData.postValue(ApiResponses(errorMessage))
            }
            isLoading.value = false
        })
        { throwable ->
            AccountWorker.initWorker(getApplication(),
                    accountName, accountType, currencyCode, iban, bic, accountNumber,
                    openingBalance, openingBalanceDate, accountRole, virtualBalance, includeInNetWorth,
                    notes, liabilityType, liabilityAmount, liabilityStartDate, interest, interestPeriod)
            apiLiveData.postValue(ApiResponses(throwable))
            isLoading.value = false
        })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
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
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorMessage)
                errorMessage = when {
                    moshi?.errors?.name != null -> moshi.errors.name[0]
                    moshi?.errors?.account_number != null -> moshi.errors.account_number[0]
                    moshi?.errors?.interest != null -> moshi.errors.interest[0]
                    moshi?.errors?.liabilityStartDate != null -> moshi.errors.liabilityStartDate[0]
                    moshi?.errors?.currency_code != null -> moshi.errors.currency_code[0]
                    moshi?.errors?.iban != null -> moshi.errors.iban[0]
                    moshi?.errors?.bic != null -> moshi.errors.bic[0]
                    moshi?.errors?.opening_balance != null -> moshi.errors.opening_balance[0]
                    moshi?.errors?.opening_balance_date != null -> moshi.errors.opening_balance_date[0]
                    moshi?.errors?.interest_period != null -> moshi.errors.interest_period[0]
                    moshi?.errors?.liability_amount != null -> moshi.errors.liability_amount[0]
                    moshi?.errors?.exception != null -> moshi.errors.exception[0]
                    else -> "Error occurred while updating Account"
                }
            }
            val networkResponse = response.body()?.data
            if (networkResponse != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.insertAccount(networkResponse)
                }.invokeOnCompletion {
                    apiLiveData.postValue(ApiResponses(response.body()))
                }
            } else {
                apiLiveData.postValue(ApiResponses(errorMessage))
            }
            isLoading.value = false
        })
        { throwable ->
            apiLiveData.postValue(ApiResponses(throwable))
            isLoading.value = false
        })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse
    }

    fun getAccountByNameAndType(accountType: String, accountName: String): LiveData<List<String>>{
        val accountData: MutableLiveData<List<String>> = MutableLiveData()
        val displayName = arrayListOf<String>()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAccountByNameAndType(accountType, accountName)
                    .distinctUntilChanged()
                    .collectLatest { accountList ->
                        accountList.forEach { accountData ->
                            displayName.add(accountData.accountAttributes?.name ?: "")
                        }
                        accountData.postValue(displayName.distinct())
                    }
        }
        return accountData
    }
}