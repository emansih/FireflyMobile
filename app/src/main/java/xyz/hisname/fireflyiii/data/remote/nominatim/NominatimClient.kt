package xyz.hisname.fireflyiii.data.remote.nominatim

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import xyz.hisname.fireflyiii.BuildConfig
import xyz.hisname.fireflyiii.repository.models.Element

class NominatimClient {

    companion object {

        @Volatile
        private var INSTANCE: Retrofit? = null

        fun getClient(): Retrofit? {
            if(INSTANCE == null){
                // Fair use policy
                val client = OkHttpClient().newBuilder()
                        .addInterceptor { chain ->
                            val request = chain.request()
                            val authenticatedRequest = request.newBuilder()
                                    .header("User-Agent", BuildConfig.APPLICATION_ID)
                                    .header("CONTACT-DETAILS", "https://github.com/emansih")
                                    .build()
                            chain.proceed(authenticatedRequest)
                        }.build()
                synchronized(NominatimClient::class.java){
                    INSTANCE = Retrofit.Builder()
                            .baseUrl("https://nominatim.openstreetmap.org")
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create(deserializer()))
                            .build()
                }
            }
            return INSTANCE
        }

        private fun deserializer(): Gson {
            return GsonBuilder()
                    .registerTypeAdapter(Array<Element>::class.java, NominatimDeserializer())
                    .create()
        }

    }
}