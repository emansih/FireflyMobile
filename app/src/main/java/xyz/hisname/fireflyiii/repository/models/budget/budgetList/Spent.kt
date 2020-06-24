package xyz.hisname.fireflyiii.repository.models.budget.budgetList

import com.google.gson.annotations.SerializedName

data class Spent(
        @SerializedName("sum")
        val amount: Double,
        val currency_code: String,
        val currency_decimal_places: Int,
        val currency_id: Int,
        val currency_symbol: String
)