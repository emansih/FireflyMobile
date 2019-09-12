package xyz.hisname.fireflyiii.repository.models.transaction

import androidx.room.*
import com.google.gson.annotations.SerializedName

data class TransactionData(
        @Ignore
        var type: String = "",
        @SerializedName("id")
        var transactionId: Long? = null,
        @Embedded
        @SerializedName("attributes")
        var transactionAttributes: TransactionAttributes? = null
)