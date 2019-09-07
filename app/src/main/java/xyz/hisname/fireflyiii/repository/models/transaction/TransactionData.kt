package xyz.hisname.fireflyiii.repository.models.transaction

import androidx.room.*
import com.google.gson.annotations.SerializedName

@Entity(tableName = "transactions")
data class TransactionData(
        @Ignore
        var type: String = "",
        @PrimaryKey(autoGenerate = false)
        var transactionId: Long? = null,
        @Embedded
        @SerializedName("attributes")
        var transactionAttributes: TransactionAttributes? = null
)