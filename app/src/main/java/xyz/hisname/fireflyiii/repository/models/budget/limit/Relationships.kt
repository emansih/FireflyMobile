package xyz.hisname.fireflyiii.repository.models.budget.limit

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName

data class Relationships(
        @Embedded
        @SerializedName("transaction_currency")
        var transactionCurrency: TransactionCurrency? = null
)