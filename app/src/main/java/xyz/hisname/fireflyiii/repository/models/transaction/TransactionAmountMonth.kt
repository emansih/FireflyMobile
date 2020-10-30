package xyz.hisname.fireflyiii.repository.models.transaction

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TransactionAmountMonth(
        val monthYear: String,
        val transactionAmount: String,
        val transactionFreq: Int
)