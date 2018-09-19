package xyz.hisname.fireflyiii.repository.viewmodel.retrofit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.AccountsService
import xyz.hisname.fireflyiii.repository.models.accounts.AccountApiResponse
import xyz.hisname.fireflyiii.util.retrofitCallback

class AccountsViewModel: ViewModel(){

    fun getAccountType(baseUrl: String, accessToken: String, type: String): LiveData<AccountApiResponse>{
        val apiResponse = MediatorLiveData<AccountApiResponse>()
        val billResponse: MutableLiveData<AccountApiResponse> = MutableLiveData()
        val accountsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(AccountsService::class.java)
        accountsService?.getAccountType(type)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                billResponse.postValue(AccountApiResponse(response.body()))
            }
        })
        { throwable ->  billResponse.postValue(AccountApiResponse(throwable))})
        apiResponse.addSource(billResponse) {
            apiResponse.value = it
        }
        return apiResponse
    }
}