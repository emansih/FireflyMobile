package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import androidx.lifecycle.*
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
import xyz.hisname.fireflyiii.util.retrofitCallback

class AccountsViewModel(application: Application) : AndroidViewModel(application){

    private val accountDatabase by lazy { AppDatabase.getInstance(application)?.accountDataDao() }
    private var  accountsService: AccountsService? = null
    private val apiLiveData: MutableLiveData<ApiResponses<AccountsModel>> = MutableLiveData()


    fun getAccounts(baseUrl: String, accessToken: String): BaseResponse<AccountData, ApiResponses<AccountsModel>> {
        val apiResponse = MediatorLiveData<ApiResponses<AccountsModel>>()
        accountsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(AccountsService::class.java)
        accountsService?.getAccountType("all")?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                response.body()?.data?.forEachIndexed { _, element ->
                    GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
                        accountDatabase?.addAccounts(element)
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

    fun getAccountType(baseUrl: String, accessToken: String, type: String): LiveData<ApiResponses<AccountsModel>>{
        val apiResponse = MediatorLiveData<ApiResponses<AccountsModel>>()
        val accountsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(AccountsService::class.java)
        accountsService?.getAccountType(type)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                apiLiveData.postValue(ApiResponses(response.body()))
            }
        })
        { throwable ->  apiLiveData.postValue(ApiResponses(throwable))})
        apiResponse.addSource(apiLiveData) {
            apiResponse.value = it
        }
        return apiResponse
    }


}