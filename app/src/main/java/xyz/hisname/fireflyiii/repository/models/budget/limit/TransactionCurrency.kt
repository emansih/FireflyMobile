package xyz.hisname.fireflyiii.repository.models.budget.limit

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName


data class TransactionCurrency(
        @Embedded
        @SerializedName("data")
        var budgetTransactionData: BudgetTransactionData? = null
)