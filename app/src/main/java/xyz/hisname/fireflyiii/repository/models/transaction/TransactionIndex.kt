package xyz.hisname.fireflyiii.repository.models.transaction

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "transactionIndexTable")
data class TransactionIndex(
        @PrimaryKey(autoGenerate = false)
        val transactionId: Long,
        val transactionJournalId: Long,
        val splitId: Long
)