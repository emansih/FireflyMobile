package xyz.hisname.fireflyiii.repository.models.budget.limits

import com.google.gson.annotations.SerializedName

data class BudgetLimitModel(
        @SerializedName("data")
        val budgetLimitData: List<BudgetLimitData>
)