package xyz.hisname.fireflyiii.repository.viewmodel.retrofit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.SettingsService
import xyz.hisname.fireflyiii.repository.api.SystemInfoService
import xyz.hisname.fireflyiii.repository.models.userinfo.settings.SettingsApiResponse
import xyz.hisname.fireflyiii.repository.models.userinfo.system.SystemApiResponse
import xyz.hisname.fireflyiii.repository.models.userinfo.user.UserInfoApiResponse
import xyz.hisname.fireflyiii.util.retrofitCallback

class UserInfoViewModel: ViewModel(){

    fun getUser(baseUrl: String?, accessToken: String?):
            LiveData<UserInfoApiResponse> {
        val apiResponse: MediatorLiveData<UserInfoApiResponse> = MediatorLiveData()
        val user: MutableLiveData<UserInfoApiResponse> = MutableLiveData()
        val systemService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(SystemInfoService::class.java)
        systemService?.getCurrentUserInfo()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                user.value = UserInfoApiResponse(response.body())
            }
        })
        { throwable ->  user.value = UserInfoApiResponse(throwable) })
        apiResponse.addSource(user) { apiResponse.value = it }
        return apiResponse
    }

    fun userSystem(baseUrl: String?, accessToken: String?):
            LiveData<SystemApiResponse> {
        val apiResponse: MediatorLiveData<SystemApiResponse> = MediatorLiveData()
        val user: MutableLiveData<SystemApiResponse> = MutableLiveData()
        val systemService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(SystemInfoService::class.java)
        systemService?.getSystemInfo()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                user.value = SystemApiResponse(response.body())
            }
        })
        { throwable ->  user.value = SystemApiResponse(throwable) })
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