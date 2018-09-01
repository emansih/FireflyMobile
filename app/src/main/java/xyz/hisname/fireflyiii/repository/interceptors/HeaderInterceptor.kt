package xyz.hisname.fireflyiii.repository.interceptors

import okhttp3.Interceptor
import xyz.hisname.fireflyiii.BuildConfig


class RetrofitHeaderInterceptor(private val accessToken: String?): Interceptor{

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val authenticatedRequest = request.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/vnd.api+json")
                .header("Accept", "application/json")
                .header("User-Agent", BuildConfig.APPLICATION_ID)
                .build()
        return chain.proceed(authenticatedRequest)
    }

}