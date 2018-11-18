package xyz.hisname.fireflyiii.data.remote

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import xyz.hisname.fireflyiii.BuildConfig
import xyz.hisname.fireflyiii.util.interceptors.HeaderInterceptor
import java.lang.StringBuilder
import java.util.*

class RetrofitBuilder {

    companion object {

        @Volatile private var INSTANCE: Retrofit? = null

        fun getClient(baseUrl: String?, accessToken: String?): Retrofit?{
            if(INSTANCE == null){
                val client =  OkHttpClient().newBuilder()
                        .addInterceptor(HeaderInterceptor(accessToken))
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
                            .baseUrl(generateUrl(baseUrl))
                            .client(client.build())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                }
            }
            return INSTANCE
        }


        fun getClient(baseUrl: String?): Retrofit?{
            if(INSTANCE == null){
                val client =  OkHttpClient().newBuilder()
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
                            .baseUrl(generateUrl(baseUrl))
                            .client(client.build())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }

        private fun generateUrl(url: String?): String{
            var modifiedUrl = ""
            if(url!!.startsWith("https")){
                // if it contains https:// remove it
                modifiedUrl = url.substring(8)
            }
            modifiedUrl = if(modifiedUrl.endsWith("/")) {
                // if it contains / at the end of url, remove it
                val stringBuilder = StringBuilder(modifiedUrl).deleteCharAt(modifiedUrl.length - 1)
                // if url has / , just let it be
                if(stringBuilder.contains("/")){
                    url
                } else {
                    "https://"  + stringBuilder.toString()
                }
            } else {
                "https://$url/"
            }
            return modifiedUrl
        }

    }
}