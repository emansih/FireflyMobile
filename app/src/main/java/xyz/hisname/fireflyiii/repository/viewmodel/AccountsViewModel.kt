package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.AccountsService
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.accounts.AccountApiResponse
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.util.retrofitCallback
import java.util.*

class AccountsViewModel(application: Application) : AndroidViewModel(application){

    private val accountDatabase by lazy { AppDatabase.getInstance(application)?.accountDataDao() }
    private var  accountsService: AccountsService? = null

    fun getAccounts(baseUrl: String, accessToken: String): AccountResponse{
        val apiResponse = MediatorLiveData<AccountApiResponse>()
        val accountResponse: MutableLiveData<AccountApiResponse> = MutableLiveData()
        accountsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(AccountsService::class.java)
        accountsService?.getAccountType("all")?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                response.body()?.data?.forEachIndexed { _, element ->
                    GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT, null, {
                        accountDatabase?.addAccounts(element)
                    })
                }
            } else {
                var errorBody = ""
                if (response.errorBody() != null) {
                    errorBody = String(response.errorBody()?.bytes()!!)
                }
                accountResponse.postValue(AccountApiResponse(errorBody))
            }
        })
        { throwable ->  accountResponse.postValue(AccountApiResponse(throwable))})
        apiResponse.addSource(accountResponse) { apiResponse.value = it }
        return AccountResponse(accountDatabase?.getAllAccounts(), apiResponse)
    }

    fun getAccountType(baseUrl: String, accessToken: String, type: String): LiveData<AccountApiResponse>{
        val apiResponse = MediatorLiveData<AccountApiResponse>()
        val accountResponse: MutableLiveData<AccountApiResponse> = MutableLiveData()
        val accountsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(AccountsService::class.java)
        accountsService?.getAccountType(type)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                accountResponse.postValue(AccountApiResponse(response.body()))
            }
        })
        { throwable ->  accountResponse.postValue(AccountApiResponse(throwable))})
        apiResponse.addSource(accountResponse) {
            apiResponse.value = it
        }
        return apiResponse
    }


}

data class AccountResponse(val databaseData: LiveData<MutableList<AccountData>>?, val apiResponse: MediatorLiveData<AccountApiResponse>)