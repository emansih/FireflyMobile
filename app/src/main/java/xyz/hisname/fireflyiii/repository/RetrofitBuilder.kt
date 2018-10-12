package xyz.hisname.fireflyiii.repository

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import xyz.hisname.fireflyiii.BuildConfig
import xyz.hisname.fireflyiii.repository.interceptors.RetrofitHeaderInterceptor
import java.util.*

class RetrofitBuilder {

    companion object {

        @Volatile private var INSTANCE: Retrofit? = null

        fun getClient(baseUrl: String?, accessToken: String?): Retrofit?{
            if(INSTANCE == null){
                val client =  OkHttpClient().newBuilder()
                        .addInterceptor(RetrofitHeaderInterceptor(accessToken))
                val certPinValue = BuildConfig.CERTPIN
                val certHost = BuildConfig.CERTHOST
                if(!certPinValue.isBlank() and !Objects.equals(certPinValue, "CHANGE THIS VALUE") and
                        certPinValue.endsWith("=") and !certHost.isBlank() and
                        !Objects.equals(certHost, "CHANGE THIS VALUE")){
                    // User enabled cert pinning
                        val certPinner = CertificatePinner.Builder()
                            .add(certHost, "sha256/$certPinValue")
                            .build()
                    client.certificatePinner(certPinner)
                }
                synchronized(RetrofitBuilder::class.java){
                    INSTANCE = Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .client(client.build())
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