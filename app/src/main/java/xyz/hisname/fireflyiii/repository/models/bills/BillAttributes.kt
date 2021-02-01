/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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