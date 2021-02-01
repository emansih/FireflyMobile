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

package xyz.hisname.fireflyiii.util.network

import okhttp3.Interceptor
import xyz.hisname.fireflyiii.BuildConfig


class HeaderInterceptor(private val accessToken: String?): Interceptor{

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