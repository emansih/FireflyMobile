package xyz.hisname.fireflyiii.repository.models.auth

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthModel(
        val token_type: String,
        val expires_in: Long,
        val access_token: String,
        val refresh_token: String
)