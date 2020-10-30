package xyz.hisname.fireflyiii.repository.models.error

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ErrorModel(
        val message: String?,
        val errors: Errors?
)