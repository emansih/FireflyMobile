package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import android.preference.PreferenceManager
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.OAuthService
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.auth.AuthModel
import xyz.hisname.fireflyiii.util.retrofitCallback
import java.util.concurrent.TimeUnit

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private var auth: MutableLiveData<ApiResponses<AuthModel>> = MutableLiveData()
    private var oAuthService: OAuthService? = null
    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(getApplication()) }

    fun getAccessToken(baseUrl: String, code: String, clientId: String, clientSecret: String): LiveData<ApiResponses<AuthModel>> {
        oAuthService = RetrofitBuilder.getClient(baseUrl, "")?.create(OAuthService::class.java)
        oAuthService?.getAccessToken(code, clientId, clientSecret, Constants.REDIRECT_URI,
                "authorization_code")?.enqueue(retrofitCallback({ response ->
            val expireTime = response.body()?.expires_in
            if(expireTime != null) {
                sharedPref.edit {
                    putString("refresh_token", response.body()?.refresh_token)
                    putString("access_token", response.body()?.access_token)
                    putLong("expires_at", (System.currentTimeMillis() +
                            TimeUnit.MINUTES.toMillis(expireTime)))
                }
            }
            auth.value = ApiResponses(response.body())})
        { throwable -> auth.value = ApiResponses(throwable)})
        return auth
    }

    fun getRefreshToken(baseUrl: String, refreshToken: String?, clientSecret: String, clientId: String): LiveData<ApiResponses<AuthModel>>{
        oAuthService = RetrofitBuilder.getClient(baseUrl, clientSecret)?.create(OAuthService::class.java)
        oAuthService?.getRefreshToken("refresh_token", refreshToken, clientId, clientSecret)?.enqueue(retrofitCallback({ response ->
            val result = response.body()
            if(result != null) {
                sharedPref.edit {
                    putString("refresh_token", result.refresh_token)
                    putString("access_token", result.access_token)
                    putLong("expires_at", (System.currentTimeMillis() +
                            TimeUnit.MINUTES.toMillis(result.expires_in)))
                }
            }
            auth.value = ApiResponses(response.body())
        })
        { throwable ->  auth.value = ApiResponses(throwable)})
        return auth
    }
}