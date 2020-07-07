package xyz.hisname.fireflyiii.repository.userinfo

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.SystemInfoService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.userinfo.system.SystemInfoModel
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
                if(userAttribute.role != null){
                    AppPref(sharedPref).userRole = userAttribute.role
                }
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
        var systemInfoModel: SystemInfoModel?  = null
        viewModelScope.launch(Dispatchers.IO){
            systemInfoModel = genericService()?.create(SystemInfoService::class.java)?.getSystemInfo()?.body()
        }.invokeOnCompletion {
            val systemData = systemInfoModel?.systemData
            if (systemData != null) {
                AppPref(sharedPref).serverVersion = systemData.version
                AppPref(sharedPref).remoteApiVersion = systemData.api_version
                AppPref(sharedPref).userOs = systemData.os
                apiOk.value = true
            } else {
                apiOk.value = false
            }
        }
        isLoading.value = false
        return apiOk
    }

}