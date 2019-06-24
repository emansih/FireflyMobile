package xyz.hisname.fireflyiii.repository.userinfo

import android.app.Application
import androidx.lifecycle.*
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.SystemInfoService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class UserInfoViewModel(application: Application) : BaseViewModel(application){

    private val systemService by lazy { genericService()?.create(SystemInfoService::class.java) }

    fun getUser(): LiveData<Boolean> {
        isLoading.value = true
        val apiOk: MutableLiveData<Boolean> = MutableLiveData()
        systemService?.getCurrentUserInfo()?.enqueue(retrofitCallback({ response ->
            val userAttribute = response.body()?.userData?.userAttributes
            if (userAttribute != null) {
                accManager.userEmail = userAttribute.email
                AppPref(sharedPref).userRole = userAttribute.role
                apiOk.value = true
            } else {
                apiOk.value = false
            }
        })
        { throwable -> apiOk.value = false })
        isLoading.value = false
        return apiOk
    }

    fun userSystem(): LiveData<Boolean> {
        isLoading.value = true
        val apiOk: MutableLiveData<Boolean> = MutableLiveData()
        systemService?.getSystemInfo()?.enqueue(retrofitCallback({ response ->
            val systemData = response.body()?.systemData
            if (systemData != null) {
                AppPref(sharedPref).serverVersion = systemData.version
                AppPref(sharedPref).remoteApiVersion = systemData.api_version
                AppPref(sharedPref).userOs = systemData.os
                apiOk.value = true
            } else {
                apiOk.value = false
            }
        })
        { throwable -> apiOk.value = false })
        isLoading.value = false
        return apiOk
    }

    fun userApiVersion(): LiveData<String>{
        isLoading.value = true
        val apiVersion: MutableLiveData<String> = MutableLiveData()
        systemService?.getSystemInfo()?.enqueue(retrofitCallback({ response ->
            val systemData = response.body()?.systemData
            if (systemData != null) {
                AppPref(sharedPref).serverVersion = systemData.version
                AppPref(sharedPref).remoteApiVersion = systemData.api_version
                AppPref(sharedPref).userOs = systemData.os
                apiVersion.value = systemData.api_version
            } else {
                apiVersion.value = AppPref(sharedPref).remoteApiVersion
            }
        })
        { throwable -> apiVersion.value = AppPref(sharedPref).remoteApiVersion })
        isLoading.value = false
        return apiVersion
    }
}