package xyz.hisname.fireflyiii.repository.models.bills

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Relation
import com.squareup.moshi.JsonClass
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@JsonClass(generateAdapter = true)
data class BillAttributes(
        var updated_at: String = "",
        var created_at: String = "",
        var name: String = "",
        var currency_id: Long = 0,
        var currency_code: String = "",
        var currency_symbol: String = "",
        var currency_decimal_places: Int = 0,
        var amount_min: BigDecimal = 0.toBigDecimal(),
        var amount_max: BigDecimal = 0.toBigDecimal(),
        var date: LocalDate = LocalDate.now(),
        var repeat_freq: String = "",
        var skip: Int = 0,
        var active: Boolean = false,
        var attachments_count: Int = 0,
        @Ignore
        var pay_dates: List<String> = listOf(),
        @Ignore
        @Relation(parentColumn = "billId", entityColumn = "billPaidId")
        var paid_dates: List<BillPaidDates> = listOf(),
        var notes: String? = "",
        var next_expected_match: String? = "",
        var isPending: Boolean = false
)