package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.work.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.AccountsService
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.BaseResponse
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.models.accounts.AccountsModel
import xyz.hisname.fireflyiii.repository.models.accounts.AccountSuccessModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.workers.account.DeleteAccountWorker
import xyz.hisname.fireflyiii.util.retrofitCallback
import java.util.*

class AccountsViewModel(application: Application) : AndroidViewModel(application){

    private val accountDatabase by lazy { AppDatabase.getInstance(application)?.accountDataDao() }
    private var accountsService: AccountsService? = null
    private val apiLiveData: MutableLiveData<ApiResponses<AccountsModel>> = MutableLiveData()


    fun getAccounts(baseUrl: String, accessToken: String): BaseResponse<AccountData, ApiResponses<AccountsModel>> {
        val apiResponse = MediatorLiveData<ApiResponses<AccountsModel>>()
        accountsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(AccountsService::class.java)
        accountsService?.getAccountType("all")?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                response.body()?.data?.forEachIndexed { _, element ->
                    GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
                        accountDatabase?.insert(element)
                    }
                }
            } else {
                var errorBody = ""
                if (response.errorBody() != null) {
                    errorBody = String(response.errorBody()?.bytes()!!)
                }
                apiLiveData.postValue(ApiResponses(errorBody))
            }
        })
        { throwable ->  apiLiveData.postValue(ApiResponses(throwable))})
        apiResponse.addSource(apiLiveData) { apiResponse.value = it }
        return BaseResponse(accountDatabase?.getAllAccounts(), apiResponse)
    }

    fun getAccountsByType(baseUrl: String, accessToken: String, type: String):
            BaseResponse<AccountData, ApiResponses<AccountsModel>>{
        val apiResponse = MediatorLiveData<ApiResponses<AccountsModel>>()
        accountsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(AccountsService::class.java)
        accountsService?.getAccountType(type)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                response.body()?.data?.forEachIndexed { _, element ->
                    GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
                        accountDatabase?.insert(element)
                    }
                }
            } else {
                var errorBody = ""
                if (response.errorBody() != null) {
                    errorBody = String(response.errorBody()?.bytes()!!)
                }
                apiLiveData.postValue(ApiResponses(errorBody))
            }
        })
        { throwable ->  apiLiveData.postValue(ApiResponses(throwable))})
        apiResponse.addSource(apiLiveData) { apiResponse.value = it }
        return BaseResponse(getDbAccount(type), apiResponse)
    }

    private fun getDbAccount(type: String): LiveData<MutableList<AccountData>>?{
        return when {
            Objects.equals(type, "asset") -> accountDatabase?.getAccountByType("Asset account")
            Objects.equals(type, "expense") -> accountDatabase?.getAccountByType("Expense account")
            Objects.equals(type, "revenue") -> accountDatabase?.getAccountByType("Revenue account")
            Objects.equals(type, "liability") -> accountDatabase?.getAccountByType("Liability account")
            else -> accountDatabase?.getAllAccounts()
        }
    }

    fun addAccounts(baseUrl: String, accessToken: String, accountName: String, accountType: String,
                    currencyCode: String, includeNetWorth: Int, accountRole: String?,
                    ccType: String?, ccMonthlyPaymentDate: String?, liabilityType: String?,
                    liabilityAmount: String?,liabilityStartDate: String?, interest: String?,
                    interestPeriod: String?,accountNumber: String?): LiveData<ApiResponses<AccountSuccessModel>>{
        val apiResponse: MediatorLiveData<ApiResponses<AccountSuccessModel>> =  MediatorLiveData()
        val apiLiveData: MutableLiveData<ApiResponses<AccountSuccessModel>> = MutableLiveData()
        accountsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(AccountsService::class.java)
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
            if (response.isSuccessful) {
                GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
                    accountDatabase?.insert(response.body()?.data!!)
                }
                apiLiveData.postValue(ApiResponses(response.body()))
            } else {
                apiLiveData.postValue(ApiResponses(errorMessage))
            }

        })
        { throwable ->  apiLiveData.postValue(ApiResponses(throwable))})
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse
    }

    fun updateAccounts(){

    }

    fun getAccountType(type: String) = accountDatabase?.getAccountsByType(type)

    fun getAccountById(id: Long) = accountDatabase?.getAccountById(id)

    fun deleteAccountById(baseUrl: String, accessToken: String, id: String): LiveData<ApiResponses<AccountsModel>>{
        val apiResponse = MediatorLiveData<ApiResponses<AccountsModel>>()
        accountsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(AccountsService::class.java)
        accountsService?.deleteAccountById(id)?.enqueue(retrofitCallback({ response ->
            if (response.code() == 204) {
                GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
                    accountDatabase?.deleteAccountById(id.toLong())
                }
                apiLiveData.postValue(ApiResponses("Delete Successful!"))
            } else {
                deleteAccount(id)
            }
        })
        { throwable ->
            deleteAccount(id)
            apiLiveData.value = ApiResponses(throwable)
        })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse
    }

    private fun deleteAccount(id: String){
        val accountTag =
                WorkManager.getInstance().getWorkInfosByTag("delete_account_$id").get()
        if(accountTag == null || accountTag.size == 0) {
            val accountData = Data.Builder()
                    .putString("id", id)
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
}