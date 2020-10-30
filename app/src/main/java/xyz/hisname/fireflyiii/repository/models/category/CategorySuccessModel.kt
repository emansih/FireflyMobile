package xyz.hisname.fireflyiii.repository.models.category

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CategorySuccessModel(
        @Json(name ="data")
        val data: CategoryData
)