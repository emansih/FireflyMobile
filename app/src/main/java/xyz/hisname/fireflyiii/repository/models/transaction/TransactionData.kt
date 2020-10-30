package xyz.hisname.fireflyiii.repository.models.transaction

import androidx.room.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TransactionData(
        @Json(name ="id")
        var transactionId: Long? = null,
        @Embedded
        @Json(name ="attributes")
        var transactionAttributes: TransactionAttributes? = null
)