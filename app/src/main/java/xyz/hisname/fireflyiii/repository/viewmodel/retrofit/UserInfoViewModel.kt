package xyz.hisname.fireflyiii.repository.viewmodel.retrofit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.SettingsService
import xyz.hisname.fireflyiii.repository.api.SystemInfoService
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.userinfo.settings.SettingsApiResponse
import xyz.hisname.fireflyiii.repository.models.userinfo.system.SystemInfoModel
import xyz.hisname.fireflyiii.repository.models.userinfo.user.UserDataModel
import xyz.hisname.fireflyiii.util.retrofitCallback

class UserInfoViewModel: ViewModel(){

    fun getUser(baseUrl: String?, accessToken: String?):
            LiveData<ApiResponses<UserDataModel>> {
        val apiResponse: MediatorLiveData<ApiResponses<UserDataModel>> = MediatorLiveData()
        val user: MutableLiveData<ApiResponses<UserDataModel>> = MutableLiveData()
        val systemService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(SystemInfoService::class.java)
        systemService?.getCurrentUserInfo()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
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
            if (response.isSuccessful) {
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