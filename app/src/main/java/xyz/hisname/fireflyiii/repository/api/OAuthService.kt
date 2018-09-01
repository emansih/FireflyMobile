package xyz.hisname.fireflyiii.repository.api

import retrofit2.Call
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.OAUTH_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.auth.AuthModel

// Link to relevant docs: https://firefly-iii.readthedocs.io/en/latest/api/start.html
interface OAuthService {

    @FormUrlEncoded
    @POST("$OAUTH_API_ENDPOINT/token")
    fun getAccessToken(@Field("code") code: String, @Field("client_id") clientId: String?,
                       @Field("client_secret") clientSecret: String?, @Field("redirect_uri") redirectUri: String,
                       @Field("grant_type") grantType: String?): Call<AuthModel>

    @FormUrlEncoded
    @POST("$OAUTH_API_ENDPOINT/token")
    fun getRefreshToken(@Field("grant_type") grantType: String?,
                        @Field("refresh_token") refreshToken: String?): Call<AuthModel>

}