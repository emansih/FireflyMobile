package xyz.hisname.fireflyiii.repository.models.bills

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Notes(
        val data: List<Any>
)