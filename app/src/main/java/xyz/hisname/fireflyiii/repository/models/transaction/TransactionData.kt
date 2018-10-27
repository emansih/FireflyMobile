package xyz.hisname.fireflyiii.repository.models.transaction

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "transactions")
data class TransactionData(
        @Ignore
        var type: String = "",
        @PrimaryKey(autoGenerate = false)
        @SerializedName("id")
        var transactionId: Long? = null,
        @Embedded
        @SerializedName("attributes")
        var transactionAttributes: TransactionAttributes? = null
)