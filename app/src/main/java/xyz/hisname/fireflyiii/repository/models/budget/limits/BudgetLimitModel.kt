package xyz.hisname.fireflyiii.repository.models.budget.limits

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BudgetLimitModel(
        @Json(name ="data")
        val budgetLimitData: List<BudgetLimitData>
)