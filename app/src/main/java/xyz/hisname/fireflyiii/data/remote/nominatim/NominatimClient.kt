/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.data.remote.nominatim

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import xyz.hisname.fireflyiii.BuildConfig

class NominatimClient {

    companion object {

        @Volatile
        private var INSTANCE: Retrofit? = null

        fun getClient(): Retrofit {
            return INSTANCE ?: synchronized(this) {
                val client = OkHttpClient().newBuilder()
                        .addInterceptor { chain ->
                            val request = chain.request()
                            val authenticatedRequest = request.newBuilder()
                                    .header("User-Agent", BuildConfig.APPLICATION_ID)
                                    .header("CONTACT-DETAILS", "https://github.com/emansih")
                                    .build()
                            chain.proceed(authenticatedRequest)
                        }.build()
                INSTANCE ?: Retrofit.Builder()
                        .baseUrl("https://nominatim.openstreetmap.org")
                        .addConverterFactory(MoshiConverterFactory.create().withNullSerialization())
                        .client(client)
                        .build()
            }
        }


        fun destroyClient(){
            INSTANCE = null
        }

    }
}