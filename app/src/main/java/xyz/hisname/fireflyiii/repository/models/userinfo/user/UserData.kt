package xyz.hisname.fireflyiii.repository.models.userinfo.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserData(
        val type: String,
        val id: String,
        @Json(name ="attributes")
        val userAttributes: UserAttributes)