package xyz.hisname.fireflyiii.repository.auth

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.remote.firefly.api.OAuthService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.auth.AuthModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.extension.isAscii
import java.security.cert.CertificateException

class AuthViewModel(application: Application): BaseViewModel(application) {

    private val isAuthenticated: MutableLiveData<Boolean> = MutableLiveData()
    val authFailedReason: MutableLiveData<String> = MutableLiveData()

    private val oAuthService by lazy { genericService()?.create(OAuthService::class.java) }

    fun getAccessToken(code: String): LiveData<Boolean> {
        authFailedReason.value = ""
        if (!code.isAscii()) {
            isAuthenticated.value = false
            authFailedReason.value = "Bearer Token contains invalid Characters!"
        } else {
            var networkCall: Response<AuthModel>? = null
            try {
                viewModelScope.launch(Dispatchers.IO) {
                    networkCall = oAuthService?.getAccessToken(code.trim(), accManager.clientId,
                            accManager.secretKey, Constants.REDIRECT_URI, "authorization_code")
                }.invokeOnCompletion {
                    val authResponse = networkCall?.body()
                    val errorBody = networkCall?.errorBody()
                    if (authResponse != null && networkCall?.isSuccessful != false) {
                        accManager.accessToken = authResponse.access_token.trim()
                        accManager.refreshToken = authResponse.refresh_token.trim()
                        accManager.tokenExpiry = authResponse.expires_in
                        accManager.authMethod = "oauth"
                        isAuthenticated.value = true
                    } else {
                        if (errorBody != null) {
                            try {
                                val errorBodyMessage = String(errorBody.bytes())
                                val gson = Gson().fromJson(errorBodyMessage, ErrorModel::class.java)
                                authFailedReason.value = gson.message
                            } catch (exception: Exception) {
                                authFailedReason.value = "Authentication Failed"
                            }
                        } else {
                            authFailedReason.value = "Authentication Failed"
                        }
                        isAuthenticated.value = false
                    }
                }
            } catch (throwable: Exception) {
                if (throwable.cause is CertificateException) {
                    if (throwable.cause?.cause?.message?.startsWith("Trust anchor for certificate") == true) {
                        authFailedReason.value = "Are you using self signed cert?"
                    } else {
                        authFailedReason.value = throwable.cause?.cause?.message
                    }
                } else {
                    authFailedReason.value = throwable.localizedMessage
                }
                isAuthenticated.value = false
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