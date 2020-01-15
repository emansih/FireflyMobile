package xyz.hisname.fireflyiii.repository.auth

import android.app.Application
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
import java.security.cert.CertificateException

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
                if(throwable.cause is CertificateException){
                    authFailedReason.value = throwable.cause?.cause?.message
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