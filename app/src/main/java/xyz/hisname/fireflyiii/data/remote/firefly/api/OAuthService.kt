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

package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.Response
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.OAUTH_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.auth.AuthModel

interface OAuthService {

    @FormUrlEncoded
    @POST("$OAUTH_API_ENDPOINT/token")
    suspend fun getAccessToken(@Field("code") code: String, @Field("client_id") clientId: String?,
                       @Field("client_secret") clientSecret: String?, @Field("redirect_uri") redirectUri: String,
                       @Field("grant_type") grantType: String? = "authorization_code"): Response<AuthModel>

    @FormUrlEncoded
    @POST("$OAUTH_API_ENDPOINT/token")
    suspend fun getRefreshToken(@Field("grant_type") grantType: String?,
                        @Field("refresh_token") refreshToken: String?,
                        @Field("client_id") clientId: String?, @Field("client_secret") clientSecret: String?): Response<AuthModel>

}