package xyz.hisname.fireflyiii.repository.viewmodel.retrofit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.OAuthService
import xyz.hisname.fireflyiii.repository.models.auth.AuthApiResponse
import xyz.hisname.fireflyiii.util.retrofitCallback

class AuthViewModel: ViewModel() {

    private var auth: MutableLiveData<AuthApiResponse> = MutableLiveData()
    private var oAuthService: OAuthService? = null

    fun getAccessToken(baseUrl: String, code: String, clientId: String, clientSecret: String): LiveData<AuthApiResponse> {
        oAuthService = RetrofitBuilder.getClient(baseUrl, "")?.create(OAuthService::class.java)
        oAuthService?.getAccessToken(code, clientId, clientSecret, Constants.REDIRECT_URI,
                "authorization_code")?.enqueue(retrofitCallback(
                { response -> auth.value = AuthApiResponse(response.body()) })
        { throwable -> auth.value = AuthApiResponse(throwable)})
        return auth
    }

    fun getRefreshToken(baseUrl: String, refreshToken: String?, clientSecret: String): LiveData<AuthApiResponse>{
        oAuthService = RetrofitBuilder.getClient(baseUrl, clientSecret)?.create(OAuthService::class.java)
        oAuthService?.getRefreshToken("refresh_token", refreshToken)?.enqueue(retrofitCallback(
                { response -> auth.value = AuthApiResponse(response.body()) })
        { throwable ->  auth.value = AuthApiResponse(throwable)})
        return auth
    }
}