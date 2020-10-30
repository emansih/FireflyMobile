package xyz.hisname.fireflyiii.repository.models.budget.limits

import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
data class BudgetLimitAttributes(
        val amount: BigDecimal,
        val budget_id: Int,
        val created_at: String,
        val currency_code: String,
        val currency_id: Int,
        val currency_name: String,
        val currency_symbol: String,
        val end: String,
        val start: String,
        val updated_at: String
)