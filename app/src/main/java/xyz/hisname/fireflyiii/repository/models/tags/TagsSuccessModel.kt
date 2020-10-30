package xyz.hisname.fireflyiii.repository.models.tags

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TagsSuccessModel(
        @Json(name ="data")
        val data: TagsData
)