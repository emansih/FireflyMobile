package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import xyz.hisname.fireflyiii.data.remote.api.RulesService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.rules.RulesApiResponse
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class RulesViewModel(application: Application): BaseViewModel(application) {

    fun getAllRules(): LiveData<RulesApiResponse>{
        val apiResponse: MediatorLiveData<RulesApiResponse> =  MediatorLiveData()
        val rules: MutableLiveData<RulesApiResponse> = MutableLiveData()
        genericService()?.create(RulesService::class.java)?.getAllRules()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                rules.value = RulesApiResponse(response.body())
            }
        })
        { throwable -> rules.value = RulesApiResponse(throwable) })
        apiResponse.addSource(rules) { apiResponse.value = it }
        return apiResponse
    }
}