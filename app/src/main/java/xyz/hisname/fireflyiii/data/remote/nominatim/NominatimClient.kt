package xyz.hisname.fireflyiii.data.remote.nominatim

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import xyz.hisname.fireflyiii.BuildConfig

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
                            .addConverterFactory(MoshiConverterFactory.create().withNullSerialization())
                            .client(client)
                            .build()
                }
            }
            return INSTANCE
        }


        fun destroyClient(){
            INSTANCE = null
        }

    }
}