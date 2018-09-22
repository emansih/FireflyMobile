package xyz.hisname.fireflyiii.repository.models.accounts

data class Attributes(
        val updated_at: String,
        val created_at: String,
        val name: String,
        val active: Boolean,
        val type: String,
        val currency_id: Int,
        val currency_code: String,
        val current_balance: Double,
        val current_balance_date: String,
        val notes: Any,
        val monthly_payment_date: Any,
        val credit_card_type: Any,
        val account_number: Any,
        val iban: String,
        val bic: Any,
        val virtual_balance: Int,
        val opening_balance: Any,
        val opening_balance_date: Any,
        val role: String
)