package xyz.hisname.fireflyiii.repository.models.transaction

import androidx.room.Embedded

data class TransactionList(
        @Embedded
        val transactions:Transactions,
        @Embedded
        val index: TransactionIndex
)