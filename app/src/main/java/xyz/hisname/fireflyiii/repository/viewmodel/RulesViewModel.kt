package xyz.hisname.fireflyiii.repository.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.RulesService
import xyz.hisname.fireflyiii.repository.models.rules.RulesApiResponse
import xyz.hisname.fireflyiii.util.retrofitCallback

class RulesViewModel: ViewModel() {

    fun getAllRules(baseUrl: String?, accessToken: String?): LiveData<RulesApiResponse>{
        val apiResponse: MediatorLiveData<RulesApiResponse> =  MediatorLiveData()
        val rules: MutableLiveData<RulesApiResponse> = MutableLiveData()
        val rulesService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(RulesService::class.java)
        rulesService?.getAllRules()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                rules.value = RulesApiResponse(response.body())
            }
        })
        { throwable ->  rules.value = RulesApiResponse(throwable)})
        apiResponse.addSource(rules) { apiResponse.value = it }
        return apiResponse
    }
}