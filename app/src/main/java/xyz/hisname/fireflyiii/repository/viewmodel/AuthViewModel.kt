package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder
import xyz.hisname.fireflyiii.data.remote.api.OAuthService
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.auth.AuthModel
import xyz.hisname.fireflyiii.util.retrofitCallback

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private var auth: MutableLiveData<ApiResponses<AuthModel>> = MutableLiveData()
    private var oAuthService: OAuthService? = null

    fun getAccessToken(baseUrl: String, code: String, clientId: String, clientSecret: String): LiveData<ApiResponses<AuthModel>> {
        oAuthService = RetrofitBuilder.getClient(baseUrl)?.create(OAuthService::class.java)
        oAuthService?.getAccessToken(code, clientId, clientSecret, Constants.REDIRECT_URI,
                "authorization_code")?.enqueue(retrofitCallback({ response ->
            val authResponse = response.body()
            if(authResponse != null) {
                AppPref(getApplication()).setRefreshToken(authResponse.refresh_token)
                AppPref(getApplication()).setAccessToken(authResponse.access_token)
                AppPref(getApplication()).setTokenExpiry(authResponse.expires_in)
            }
            auth.value = ApiResponses(response.body())})
        { throwable -> auth.value = ApiResponses(throwable)})
        return auth
    }

    fun getRefreshToken(baseUrl: String, refreshToken: String?, clientSecret: String, clientId: String): LiveData<ApiResponses<AuthModel>>{
        oAuthService = RetrofitBuilder.getClient(baseUrl, clientSecret)?.create(OAuthService::class.java)
        oAuthService?.getRefreshToken("refresh_token", refreshToken, clientId, clientSecret)?.enqueue(retrofitCallback({ response ->
            val authResponse = response.body()
            if(authResponse != null) {
                AppPref(getApplication()).setRefreshToken(authResponse.refresh_token)
                AppPref(getApplication()).setAccessToken(authResponse.access_token)
                AppPref(getApplication()).setTokenExpiry(authResponse.expires_in)
            }
            auth.value = ApiResponses(response.body())
        })
        { throwable ->  auth.value = ApiResponses(throwable)})
        return auth
    }
}