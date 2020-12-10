package xyz.hisname.fireflyiii.repository.models.transaction

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TransactionAttributes(
        val created_at: String,
        val group_title: String,
        val transactions: List<Transactions>,
        val updated_at: String,
        val user: Int
)