package xyz.hisname.fireflyiii.repository.models.budget.limit

import androidx.room.Ignore
import com.google.gson.annotations.SerializedName

data class BudgetTransactionData(
        @Ignore
        @SerializedName("type")
        var relationshipType: String = "",
        @SerializedName("id")
        var currencyId: Long = 0
)