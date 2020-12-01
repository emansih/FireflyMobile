package xyz.hisname.fireflyiii.repository.userinfo

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.SystemInfoService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.userinfo.system.SystemInfoModel

class UserInfoViewModel(application: Application) : BaseViewModel(application){

    fun userSystem(): LiveData<Boolean> {
        isLoading.postValue(true)
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
                apiOk.postValue(true)
            } else {
                apiOk.postValue(false)
            }
        }
        isLoading.postValue(false)
        return apiOk
    }

}