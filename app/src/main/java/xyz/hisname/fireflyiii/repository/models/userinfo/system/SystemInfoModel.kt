package xyz.hisname.fireflyiii.repository.models.userinfo.system

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SystemInfoModel(
        @Json(name ="data")
        val systemData: SystemData
)