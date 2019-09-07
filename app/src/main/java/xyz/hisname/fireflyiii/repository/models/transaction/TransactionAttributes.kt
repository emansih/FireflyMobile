package xyz.hisname.fireflyiii.repository.models.transaction

import androidx.room.Entity

@Entity
data class TransactionAttributes(
        var created_at: String = "",
        var group_title: String = "",
        var transactions: List<Transactions>? = null,
        var updated_at: String = "",
        var user: Int = 0
)