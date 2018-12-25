package xyz.hisname.fireflyiii.repository.models.piggy

import androidx.room.Entity
import java.math.BigDecimal

@Entity
data class PiggyAttributes(
        val updated_at: String,
        val created_at: String,
        val name: String,
        val account_id: Long,
        val currency_id: Int,
        val currency_code: String,
        val currency_symbol: String,
        val currency_dp: Int,
        val target_amount: BigDecimal,
        val percentage: Int,
        val current_amount: BigDecimal,
        val left_to_save: BigDecimal,
        val save_per_month: BigDecimal,
        val start_date: String?,
        val target_date: String?,
        val order: Int?,
        val active: Boolean,
        val notes: String?
)