package xyz.hisname.fireflyiii.repository.models.transaction

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TransactionModel(
        val data: MutableCollection<TransactionData>,
        val meta: Meta
)