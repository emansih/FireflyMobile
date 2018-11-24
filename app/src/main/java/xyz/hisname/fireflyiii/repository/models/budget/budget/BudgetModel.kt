package xyz.hisname.fireflyiii.repository.models.budget.budget

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName

data class BudgetModel(
        @Embedded
        @SerializedName("data")
        val budgetData: List<BudgetData>,
        val meta: Meta
)