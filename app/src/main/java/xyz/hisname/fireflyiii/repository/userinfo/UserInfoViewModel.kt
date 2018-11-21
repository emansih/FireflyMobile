package xyz.hisname.fireflyiii.repository.userinfo

import android.app.Application
import androidx.lifecycle.*
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder
import xyz.hisname.fireflyiii.data.remote.api.SystemInfoService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.util.retrofitCallback

class UserInfoViewModel(application: Application) : BaseViewModel(application){

    fun getUser(): LiveData<Boolean> {
        isLoading.value = true
        val apiOk: MutableLiveData<Boolean> = MutableLiveData()
        val systemService = RetrofitBuilder.getClient(AppPref(getApplication()).baseUrl,
                 AppPref(getApplication()).accessToken)?.create(SystemInfoService::class.java)
        systemService?.getCurrentUserInfo()?.enqueue(retrofitCallback({ response ->
            val userAttribute = response.body()?.userData?.userAttributes
            if (userAttribute != null) {
                AppPref(getApplication()).userEmail = userAttribute.email
                AppPref(getApplication()).userRole = userAttribute.role
                apiOk.value = true
            } else {
                apiOk.value = false
            }
        })
        { throwable -> apiOk.value = false})
        isLoading.value = false
        return apiOk
    }

    fun userSystem(): LiveData<Boolean> {
        isLoading.value = true
        val apiOk: MutableLiveData<Boolean> = MutableLiveData()
        val systemService = RetrofitBuilder.getClient(AppPref(getApplication()).baseUrl,
                AppPref(getApplication()).accessToken)?.create(SystemInfoService::class.java)
        systemService?.getSystemInfo()?.enqueue(retrofitCallback({ response ->
            val systemData = response.body()?.systemData
            if (systemData != null) {
                AppPref(getApplication()).serverVersion = systemData.version
                AppPref(getApplication()).remoteApiVersion = systemData.api_version
                AppPref(getApplication()).userOs = systemData.os
                apiOk.value = true
            } else {
                apiOk.value = false
            }
        })
        { throwable ->  apiOk.value = false })
        isLoading.value = false
        return apiOk
    }
}