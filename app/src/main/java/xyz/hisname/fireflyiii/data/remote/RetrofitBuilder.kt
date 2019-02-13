package xyz.hisname.fireflyiii.data.remote

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import xyz.hisname.fireflyiii.util.LocalDateTimeConverter
import xyz.hisname.fireflyiii.util.network.HeaderInterceptor
import java.net.MalformedURLException
import java.net.URL

class RetrofitBuilder {

    companion object {

        @Volatile private var INSTANCE: Retrofit? = null
        private lateinit var baseUrl: URL

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
                            .addConverterFactory(GsonConverterFactory.create(convertIso8601()))
                            .build()
                }
            }
            return INSTANCE
        }

        private fun convertIso8601(): Gson {
            return GsonBuilder()
                    .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeConverter())
                    .enableComplexMapKeySerialization()
                    .serializeNulls()
                    .setPrettyPrinting()
                    .setVersion(1.0)
                    .create()
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
            val initialUrl = if(url.endsWith("/")){
                // Remove / if user has it
                StringBuilder(url).deleteCharAt(url.length - 1).toString()
            } else {
                url
            }
            baseUrl = try {
                URL(initialUrl)
            } catch (malformed: MalformedURLException){
                URL("https://$initialUrl")
            }
            val basePort = if(baseUrl.port == -1){
                // User has no port in base Url.
                //  Example: https://demo.firefly-iii.org
                ""
            } else {
                // User has port in base Url.
                //  Example: https://demo.firefly-iii.org:1234
                ":" + baseUrl.port
            }
            val baseProtocol = baseUrl.protocol
            // Remove protocol. Example: https://demo.firefly-iii.org becomes demo.firefly-iii.org
            val baseUrlHost = baseUrl.host
            val apiUrl = if(baseUrl.path.isEmpty()){
                // User has no path in url(demo.firefly-iii.org)
                baseUrlHost + basePort
            } else {
                // User has path in url(demo.firefly-iii.org/login)
                baseUrlHost + basePort + baseUrl.path
            }
            return "$baseProtocol://$apiUrl/"
        }

    }
}