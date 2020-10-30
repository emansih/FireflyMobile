package xyz.hisname.fireflyiii.repository.models.userinfo.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDataModel(
        @Json(name ="data")
        val userData: UserData
)