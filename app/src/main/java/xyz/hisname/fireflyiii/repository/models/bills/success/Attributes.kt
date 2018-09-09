package xyz.hisname.fireflyiii.repository.models.bills.success

data class Attributes(
        val updated_at: String,
        val created_at: String,
        val name: String,
        val currency_id: Int,
        val currency_code: String,
        val currency_symbol: String,
        val amount_min: Int,
        val amount_max: Int,
        val date: String,
        val repeat_freq: String,
        val skip: Int,
        val automatch: Boolean,
        val active: Boolean,
        val attachments_count: Int,
        val pay_dates: List<Any>,
        val paid_dates: List<Any>,
        val next_expected_match: Any
)