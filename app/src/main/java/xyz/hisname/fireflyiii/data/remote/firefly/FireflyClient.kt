package xyz.hisname.fireflyiii.data.remote.firefly

import android.net.Uri
import android.util.Base64
import com.squareup.moshi.Moshi
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import xyz.hisname.fireflyiii.data.remote.firefly.moshi.*
import xyz.hisname.fireflyiii.util.network.HeaderInterceptor
import java.net.MalformedURLException
import java.net.URL
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

class FireflyClient {

    companion object {

        @Volatile private var INSTANCE: Retrofit? = null
        private lateinit var baseUrl: URL

        fun getClient(baseUrl: String, accessToken: String, certPinValue: String,
                      trustManager: X509TrustManager?, sslSocketFactory: SSLSocketFactory?): Retrofit{
            return INSTANCE ?: synchronized(this){
                val client = OkHttpClient().newBuilder()
                        .addInterceptor(HeaderInterceptor(accessToken))
                        .callTimeout(1, TimeUnit.MINUTES)
                        .connectTimeout(1, TimeUnit.MINUTES)
                        .writeTimeout(1, TimeUnit.MINUTES)
                        .readTimeout(1, TimeUnit.MINUTES)
                if(trustManager != null && sslSocketFactory != null) {
                    client.sslSocketFactory(sslSocketFactory, trustManager)
                    client.hostnameVerifier { hostname, session -> true }
                }
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
                val moshi = Moshi.Builder()
                        .add(NullToEmptyStringConverter)
                        .add(JsonObjectConverter)
                        .add(LocalDate::class.java, LocalDateAdapter())
                        .add(OffsetDateTime::class.java, OffsetDateTimeConverter())
                        .add(BigDecimalConverter())
                        .add(Uri::class.java, UriConverter())
                        .build()
                INSTANCE ?: Retrofit.Builder()
                        .baseUrl(generateUrl(baseUrl))
                        .client(client.build())
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .addConverterFactory(MoshiConverterFactory.create(moshi))
                        .build().also { INSTANCE = it }
            }
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