package xyz.hisname.fireflyiii.repository.models.bills

import androidx.room.Entity
import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@Entity
@JsonClass(generateAdapter = true)
data class BillAttributes(
        val updated_at: String,
        val created_at: String,
        var name: String,
        val currency_id: Long,
        val currency_code: String,
        val currency_symbol: String,
        val currency_decimal_places: Int,
        val amount_min: BigDecimal,
        val amount_max: BigDecimal,
        val date: String,
        val repeat_freq: String,
        val skip: Int,
        val active: Boolean,
        val attachments_count: Int = 0,
        val pay_dates: List<String>,
        val paid_dates: List<String>,
        val notes: String?,
        val next_expected_match: String?,
        val isPending: Boolean = false
)