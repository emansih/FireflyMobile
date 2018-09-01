package xyz.hisname.fireflyiii.repository

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import xyz.hisname.fireflyiii.repository.interceptors.RetrofitHeaderInterceptor

class RetrofitBuilder {

    companion object {

        @Volatile private var INSTANCE: Retrofit? = null

        fun getClient(baseUrl: String?, accessToken: String?): Retrofit?{
            if(INSTANCE == null){
                val client = OkHttpClient().newBuilder()
                        .addInterceptor(RetrofitHeaderInterceptor(accessToken))
                        .build()
                synchronized(RetrofitBuilder::class.java){
                    INSTANCE = Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                }

            }
            return INSTANCE
        }

        fun destroyInstance() {
            RetrofitBuilder.INSTANCE = null
        }

    }
}