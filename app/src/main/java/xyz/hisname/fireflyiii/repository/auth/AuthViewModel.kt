package xyz.hisname.fireflyiii.repository.auth

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.remote.firefly.api.OAuthService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.auth.AuthModel
import xyz.hisname.fireflyiii.util.extension.isAscii
import java.net.UnknownServiceException
import java.security.cert.CertificateException

class AuthViewModel(application: Application): BaseViewModel(application) {

    private val isAuthenticated: MutableLiveData<Boolean> = MutableLiveData()
    val authFailedReason: MutableLiveData<String> = MutableLiveData()

    private val oAuthService by lazy { genericService()?.create(OAuthService::class.java) }

    fun getAccessToken(code: String): LiveData<Boolean> {
        authFailedReason.postValue("")
        if (!code.isAscii()) {
            isAuthenticated.postValue(false)
            authFailedReason.postValue("Bearer Token contains invalid Characters!")
        } else {
            var networkCall: Response<AuthModel>? = null
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    networkCall = oAuthService?.getAccessToken(code.trim(), accManager.clientId,
                            accManager.secretKey, Constants.REDIRECT_URI)
                    val authResponse = networkCall?.body()
                    val errorBody = networkCall?.errorBody()
                    if (authResponse != null && networkCall?.isSuccessful != false) {
                        accManager.accessToken = authResponse.access_token.trim()
                        accManager.refreshToken = authResponse.refresh_token.trim()
                        accManager.tokenExpiry = authResponse.expires_in
                        accManager.authMethod = "oauth"
                        isAuthenticated.postValue(true)
                    } else if(errorBody != null){
                        val errorBodyMessage = String(errorBody.bytes())
                        authFailedReason.postValue(errorBodyMessage)
                        isAuthenticated.postValue(false)
                    }
                } catch (exception: UnknownServiceException){
                    authFailedReason.postValue("http is not supported. Please use https")
                    isAuthenticated.postValue(false)
                } catch (certificateException: CertificateException){
                    authFailedReason.postValue("Are you using self signed cert?")
                    isAuthenticated.postValue(false)
                } catch (exception: Exception){
                    authFailedReason.postValue(exception.localizedMessage)
                    isAuthenticated.postValue(false)
                }
            }
        }
        return isAuthenticated
    }

        fun getRefreshToken(): LiveData<Boolean> {
            var networkCall: Response<AuthModel>? = null
            try {
                viewModelScope.launch(Dispatchers.IO) {
                    networkCall = oAuthService?.getRefreshToken("refresh_token",
                            accManager.refreshToken, accManager.clientId,
                            accManager.secretKey)
                }.invokeOnCompletion {
                    val authResponse = networkCall?.body()
                    if (authResponse != null && networkCall?.isSuccessful != false) {
                        accManager.accessToken = authResponse.access_token
                        accManager.refreshToken = authResponse.refresh_token
                        accManager.tokenExpiry = authResponse.expires_in
                        isAuthenticated.value = true
                    } else {
                        isAuthenticated.value = false
                    }
                }
            } catch (exception: Exception) {
                isAuthenticated.value = false
            }
            return isAuthenticated
        }
    }