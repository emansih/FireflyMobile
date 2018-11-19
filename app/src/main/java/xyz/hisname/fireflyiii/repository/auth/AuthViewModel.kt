package xyz.hisname.fireflyiii.repository.auth

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder
import xyz.hisname.fireflyiii.data.remote.api.OAuthService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.UserRepository
import xyz.hisname.fireflyiii.util.retrofitCallback

class AuthViewModel(application: Application): BaseViewModel(application){

    private val userRepo: UserRepository = UserRepository(AppPref(application))
    private val isAuthenticated: MutableLiveData<Boolean> = MutableLiveData()

    fun getAccessToken(code: String): LiveData<Boolean> {
        isLoading.value = true
        val oAuthService = RetrofitBuilder.getClient(userRepo.baseUrl)?.create(OAuthService::class.java)
        oAuthService?.getAccessToken(code, userRepo.clientId, userRepo.clientSecret, Constants.REDIRECT_URI,
                "authorization_code")?.enqueue(retrofitCallback({ response ->
            val authResponse = response.body()
            if(authResponse != null) {
                userRepo.insertAccessToken(authResponse.access_token)
                userRepo.insertRefreshToken(authResponse.refresh_token)
                userRepo.insertTokenExpiry(authResponse.expires_in)
                isAuthenticated.value = true
            } else {
                isAuthenticated.value = false
            }
        })
        { throwable -> isAuthenticated.value = false })
        isLoading.value = false
        return isAuthenticated
    }

    fun getRefreshToken(): LiveData<Boolean>{
        val oAuthService = RetrofitBuilder.getClient(userRepo.baseUrl, userRepo.clientSecret)?.create(OAuthService::class.java)
        oAuthService?.getRefreshToken("refresh_token", userRepo.refreshToken,
                userRepo.clientId, userRepo.clientSecret)?.enqueue(retrofitCallback({ response ->
            val authResponse = response.body()
            if(authResponse != null) {
                userRepo.insertAccessToken(authResponse.access_token)
                userRepo.insertRefreshToken(authResponse.refresh_token)
                userRepo.insertTokenExpiry(authResponse.expires_in)
                isAuthenticated.value = true
            } else {
                isAuthenticated.value = false
            }
        })
        { throwable -> isAuthenticated.value = false })
        return isAuthenticated
    }
}