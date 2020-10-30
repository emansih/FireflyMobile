package xyz.hisname.fireflyiii.repository.models.bills

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BillSuccessModel(
        @Json(name ="data")
        val data: BillData
)