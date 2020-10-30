package xyz.hisname.fireflyiii.repository.models.tags

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Meta(
        val pagination: Pagination
)