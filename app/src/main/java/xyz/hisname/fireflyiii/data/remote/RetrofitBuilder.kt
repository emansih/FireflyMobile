package xyz.hisname.fireflyiii.data.remote

import android.util.Base64
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import xyz.hisname.fireflyiii.util.network.HeaderInterceptor
import java.lang.StringBuilder

class RetrofitBuilder {

    companion object {

        @Volatile private var INSTANCE: Retrofit? = null

        fun getClient(baseUrl: String, accessToken: String, certPinValue: String): Retrofit?{
            if(INSTANCE == null){
                val client = OkHttpClient().newBuilder()
                        .addInterceptor(HeaderInterceptor(accessToken))
                if(!certPinValue.isBlank()){
                    try {
                        val certPinner = CertificatePinner.Builder()
                                .add(baseUrl, "sha256/" +
                                        Base64.decode(certPinValue, Base64.DEFAULT)
                                                .toString(Charsets.UTF_8))
                                .build()
                        client.certificatePinner(certPinner)
                    } catch (exception: IllegalArgumentException){ }
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



        fun getClient(baseUrl: String): Retrofit?{
            if(INSTANCE == null){
                synchronized(RetrofitBuilder::class.java){
                    INSTANCE = Retrofit.Builder()
                            .baseUrl(generateUrl(baseUrl))
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }

        private fun generateUrl(url: String): String{
            var modifiedUrl = ""
            if(url.startsWith("https")){
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