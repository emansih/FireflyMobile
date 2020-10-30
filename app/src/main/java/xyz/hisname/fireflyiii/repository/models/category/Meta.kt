package xyz.hisname.fireflyiii.repository.models.category

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Meta(
        val pagination: Pagination
)