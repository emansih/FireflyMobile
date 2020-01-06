package xyz.hisname.fireflyiii.repository.auth

import android.app.Application
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.OAuthService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.extension.isAscii
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import java.security.cert.CertPathValidatorException

class AuthViewModel(application: Application): BaseViewModel(application) {

    private val isAuthenticated: MutableLiveData<Boolean> = MutableLiveData()
    val authFailedReason: MutableLiveData<String> = MutableLiveData()

    fun getAccessToken(code: String): LiveData<Boolean> {
        authFailedReason.value = ""
        if(!code.isAscii()){
            isAuthenticated.value = false
            authFailedReason.value = "Bearer Token contains invalid Characters!"
        } else {
            val oAuthService = FireflyClient.getClient(AppPref(sharedPref).baseUrl)?.create(OAuthService::class.java)
            oAuthService?.getAccessToken(code.trim(), accManager.clientId, accManager.secretKey, Constants.REDIRECT_URI,
                    "authorization_code")?.enqueue(retrofitCallback({ response ->
                val authResponse = response.body()
                val errorBody = response.errorBody()
                if (authResponse != null) {
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
            })
            { throwable ->
                if(throwable.cause is CertPathValidatorException){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                        if(CertPathValidatorException().reason == CertPathValidatorException.BasicReason.EXPIRED){
                            authFailedReason.value = "Your SSL certificate has expired"
                        } else if(CertPathValidatorException().reason == CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED){
                            authFailedReason.value = "The public key or the signature algorithm has been constrained"
                        } else if(CertPathValidatorException().reason == CertPathValidatorException.BasicReason.INVALID_SIGNATURE){
                            authFailedReason.value = "Your SSL certificate has invalid signature"
                        } else if(CertPathValidatorException().reason == CertPathValidatorException.BasicReason.NOT_YET_VALID){
                            authFailedReason.value = "Your SSL certificate is not yet valid"
                        } else if(CertPathValidatorException().reason == CertPathValidatorException.BasicReason.REVOKED){
                            authFailedReason.value = "Your SSL certificate has been revoked"
                        } else {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                                authFailedReason.value = "Are you using a self signed cert? Android P doesn't support it out of the box"
                            } else {
                                authFailedReason.value = throwable.localizedMessage
                            }
                        }
                    } else {
                        authFailedReason.value = throwable.localizedMessage
                    }
                } else {
                    authFailedReason.value = throwable.localizedMessage
                }
                isAuthenticated.value = false
            })
        }
        return isAuthenticated
    }

    fun getRefreshToken(): LiveData<Boolean> {
        genericService()?.create(OAuthService::class.java)?.getRefreshToken("refresh_token",
                accManager.refreshToken, accManager.clientId,
                accManager.secretKey)?.enqueue(retrofitCallback({ response ->
            val authResponse = response.body()
            if (authResponse != null) {
                accManager.accessToken = authResponse.access_token
                accManager.refreshToken = authResponse.refresh_token
                accManager.tokenExpiry = authResponse.expires_in
                isAuthenticated.value = true
            } else {
                isAuthenticated.value = false
            }
        })
        { throwable -> isAuthenticated.value = false })
        return isAuthenticated
    }
}