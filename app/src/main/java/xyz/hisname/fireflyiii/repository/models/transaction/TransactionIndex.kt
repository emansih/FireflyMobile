package xyz.hisname.fireflyiii.repository.models.transaction

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey



@Entity(tableName = "transactionIndexTable", foreignKeys = [ForeignKey(entity = Transactions::class,
        parentColumns = arrayOf("transaction_journal_id"), childColumns = arrayOf("transactionJournalId"),
        onDelete = ForeignKey.CASCADE)])
data class TransactionIndex(
        @PrimaryKey(autoGenerate = false)
        var transactionId: Long?,
        var transactionJournalId: Long?
)