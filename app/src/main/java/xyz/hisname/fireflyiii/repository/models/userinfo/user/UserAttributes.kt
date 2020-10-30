package xyz.hisname.fireflyiii.repository.models.userinfo.user

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserAttributes(
        val updated_at: String,
        val created_at: String,
        val email: String,
        val blocked: Boolean,
        val blocked_code: String,
        val role: String?
)