package xyz.hisname.fireflyiii.repository.models.budget.budgetList

data class Spent(
        val amount: Double,
        val currency_code: String,
        val currency_decimal_places: Int,
        val currency_id: Int,
        val currency_symbol: String
)