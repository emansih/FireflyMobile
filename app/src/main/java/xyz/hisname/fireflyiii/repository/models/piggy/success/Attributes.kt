package xyz.hisname.fireflyiii.repository.models.piggy.success

data class Attributes(
        val updated_at: String,
        val created_at: String,
        val name: String,
        val currency_id: Int,
        val currency_code: String,
        val currency_symbol: String,
        val currency_dp: Int,
        val target_amount: Int,
        val percentage: Int,
        val current_amount: Int,
        val left_to_save: Int,
        val save_per_month: Int,
        val start_date: Any,
        val target_date: Any,
        val order: Int,
        val active: Boolean,
        val notes: Any
)