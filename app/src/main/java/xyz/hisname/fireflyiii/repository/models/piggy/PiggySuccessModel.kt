package xyz.hisname.fireflyiii.repository.models.piggy

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PiggySuccessModel(
        @Json(name ="data")
        val data: PiggyData
)