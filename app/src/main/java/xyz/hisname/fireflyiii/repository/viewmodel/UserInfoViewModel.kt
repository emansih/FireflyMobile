package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import androidx.lifecycle.*
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder
import xyz.hisname.fireflyiii.data.remote.api.SettingsService
import xyz.hisname.fireflyiii.data.remote.api.SystemInfoService
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.userinfo.settings.SettingsApiResponse
import xyz.hisname.fireflyiii.repository.models.userinfo.system.SystemInfoModel
import xyz.hisname.fireflyiii.repository.models.userinfo.user.UserDataModel
import xyz.hisname.fireflyiii.util.retrofitCallback

class UserInfoViewModel(application: Application) : AndroidViewModel(application){

    fun getUser(baseUrl: String?, accessToken: String?):
            LiveData<ApiResponses<UserDataModel>> {
        val apiResponse: MediatorLiveData<ApiResponses<UserDataModel>> = MediatorLiveData()
        val user: MutableLiveData<ApiResponses<UserDataModel>> = MutableLiveData()
        val systemService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(SystemInfoService::class.java)
        systemService?.getCurrentUserInfo()?.enqueue(retrofitCallback({ response ->
            val userAttribute = response.body()?.userData?.userAttributes
            if (userAttribute != null) {
                AppPref(getApplication()).setUserEmail(userAttribute.email)
                AppPref(getApplication()).setUserRole(userAttribute.role)
                user.value = ApiResponses(response.body())
            }
        })
        { throwable ->  user.value = ApiResponses(throwable) })
        apiResponse.addSource(user) { apiResponse.value = it }
        return apiResponse
    }

    fun userSystem(baseUrl: String?, accessToken: String?):
            LiveData<ApiResponses<SystemInfoModel>> {
        val apiResponse: MediatorLiveData<ApiResponses<SystemInfoModel>> = MediatorLiveData()
        val user: MutableLiveData<ApiResponses<SystemInfoModel>> = MutableLiveData()
        val systemService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(SystemInfoService::class.java)
        systemService?.getSystemInfo()?.enqueue(retrofitCallback({ response ->
            val systemData = response.body()?.systemData
            if (systemData != null) {
                AppPref(getApplication()).setServerVersion(systemData.version)
                AppPref(getApplication()).setRemoteApiVersion(systemData.api_version)
                AppPref(getApplication()).setUserOS(systemData.os)
                user.value = ApiResponses(response.body())
            }
        })
        { throwable ->  user.value = ApiResponses(throwable) })
        apiResponse.addSource(user) { apiResponse.value = it }
        return apiResponse
    }

    fun getUserSettings(baseUrl: String?, accessToken: String?):
            LiveData<SettingsApiResponse> {
        val apiResponse: MediatorLiveData<SettingsApiResponse> = MediatorLiveData()
        val user: MutableLiveData<SettingsApiResponse> = MutableLiveData()
        val settingsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(SettingsService::class.java)
        settingsService?.getSettings()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                user.value = SettingsApiResponse(response.body())
            }
        })
        { throwable ->  user.value = SettingsApiResponse(throwable) })
        apiResponse.addSource(user) { apiResponse.value = it }
        return apiResponse
    }


}