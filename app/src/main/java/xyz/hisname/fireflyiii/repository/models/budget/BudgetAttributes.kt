package xyz.hisname.fireflyiii.repository.models.budget

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
data class BudgetAttributes(
        val created_at: String,
        val updated_at: String,
        val currency_id: Int,
        val currency_code: String,
        val currency_symbol: String,
        val currency_decimal_places: Int,
        val amount: BigDecimal,
        @Json(name ="start")
        val start_date: String,
        @Json(name ="end")
        val end_date: String
)