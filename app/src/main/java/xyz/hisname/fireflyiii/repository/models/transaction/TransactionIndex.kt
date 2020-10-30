package xyz.hisname.fireflyiii.repository.models.transaction

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "transactionIndexTable")
data class TransactionIndex(
        @PrimaryKey(autoGenerate = false)
        var transactionId: Long?,
        var transactionJournalId: Long?
)