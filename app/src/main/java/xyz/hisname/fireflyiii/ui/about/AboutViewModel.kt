package xyz.hisname.fireflyiii.ui.about

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.SystemInfoService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.userinfo.SystemInfoRepository

class AboutViewModel(application: Application): BaseViewModel(application) {

    private val systemInfoRepository by lazy { SystemInfoRepository(
            genericService().create(SystemInfoService::class.java),
            sharedPref,
            accManager)
    }

    private val appPref by lazy { AppPref(sharedPref) }

    var serverVersion = appPref.serverVersion
    var apiVersion = appPref.remoteApiVersion
    var userOs = appPref.userOs

    fun userSystem(): LiveData<Boolean> {
        val apiOk: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO+ CoroutineExceptionHandler { _, throwable ->
            apiOk.postValue(false)
        }){
            systemInfoRepository.getUserSystem()
            serverVersion = appPref.serverVersion
            apiVersion = appPref.remoteApiVersion
            userOs = appPref.userOs
            apiOk.postValue(true)
        }
        return apiOk
    }
}