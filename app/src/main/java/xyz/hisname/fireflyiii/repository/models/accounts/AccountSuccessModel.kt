package xyz.hisname.fireflyiii.repository.models.accounts

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AccountSuccessModel(
        @Json(name ="data")
        val data: AccountData
)