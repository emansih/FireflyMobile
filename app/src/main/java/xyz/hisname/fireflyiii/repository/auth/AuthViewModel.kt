package xyz.hisname.fireflyiii.repository.auth

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.OAuthService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.auth.AuthModel

class AuthViewModel(application: Application): BaseViewModel(application) {

    private val isAuthenticated: MutableLiveData<Boolean> = MutableLiveData()

    private val oAuthService by lazy { genericService()?.create(OAuthService::class.java) }

    fun getRefreshToken(): LiveData<Boolean> {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val networkCall = oAuthService?.getRefreshToken("refresh_token",
                        accManager.refreshToken, accManager.clientId,
                        accManager.secretKey)
                val authResponse = networkCall?.body()
                if (authResponse != null && networkCall.isSuccessful) {
                    accManager.accessToken = authResponse.access_token
                    accManager.refreshToken = authResponse.refresh_token
                    accManager.tokenExpiry = authResponse.expires_in
                    isAuthenticated.postValue(true)
                    FireflyClient.destroyInstance()
                } else {
                    isAuthenticated.postValue(false)
                }
            }
        } catch (exception: Exception) {
            isAuthenticated.postValue(false)
        }
        return isAuthenticated
    }
}