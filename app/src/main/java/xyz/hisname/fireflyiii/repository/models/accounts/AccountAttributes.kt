package xyz.hisname.fireflyiii.repository.models.accounts

import androidx.room.Entity

@Entity
data class AccountAttributes(
        val updated_at: String,
        val created_at: String,
        val name: String,
        val active: Boolean,
        val type: String,
        val account_role: String?,
        val currency_id: Int?,
        val currency_code: String?,
        val current_balance: Double,
        val currency_symbol: String?,
        val current_balance_date: String,
        val notes: String?,
        val monthly_payment_date: String?,
        val credit_card_type: String?,
        val account_number: String?,
        val iban: String?,
        val bic: String?,
        val virtual_balance: Double?,
        val opening_balance: String?,
        val opening_balance_date: String?,
        val liability_type: String?,
        val liability_amount: String?,
        val liability_start_date: String?,
        val interest: String?,
        val interest_period: String?,
        val include_net_worth: Boolean
)