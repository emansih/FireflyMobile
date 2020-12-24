package xyz.hisname.fireflyiii.repository.models.transaction

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "transactionIndexTable", foreignKeys = [ForeignKey(entity = Transactions::class,
        parentColumns = arrayOf("transaction_journal_id"), childColumns = arrayOf("transactionJournalId"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE)])
data class TransactionIndex(
        @PrimaryKey(autoGenerate = true)
        val tableId: Long = 0,
        val transactionId: Long,
        val transactionJournalId: Long,
        val groupTitle: String
)