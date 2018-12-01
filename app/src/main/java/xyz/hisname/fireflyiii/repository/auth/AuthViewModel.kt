package xyz.hisname.fireflyiii.repository.auth

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder
import xyz.hisname.fireflyiii.data.remote.api.OAuthService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.util.retrofitCallback

class AuthViewModel(application: Application): BaseViewModel(application) {

    private val isAuthenticated: MutableLiveData<Boolean> = MutableLiveData()

    fun getAccessToken(code: String): LiveData<Boolean> {
        isLoading.value = true
        val oAuthService = RetrofitBuilder.getClient(AppPref(sharedPref).baseUrl)?.create(OAuthService::class.java)
        oAuthService?.getAccessToken(code, accManager.clientId, accManager.secretKey, Constants.REDIRECT_URI,
                "authorization_code")?.enqueue(retrofitCallback({ response ->
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
        isLoading.value = false
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