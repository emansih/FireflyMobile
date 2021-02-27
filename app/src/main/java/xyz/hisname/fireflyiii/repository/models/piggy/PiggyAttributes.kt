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

package xyz.hisname.fireflyiii.repository.models.piggy

import androidx.room.Entity
import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@Entity
@JsonClass(generateAdapter = true)
data class PiggyAttributes(
        val updated_at: String?,
        val created_at: String?,
        val name: String,
        val account_id: Long?,
        val account_name: String?,
        val currency_id: Long?,
        val currency_code: String?,
        val currency_symbol: String?,
        val currency_dp: Int?,
        val target_amount: BigDecimal?,
        val percentage: Int?,
        val current_amount: BigDecimal?,
        val left_to_save: BigDecimal?,
        val save_per_month: BigDecimal?,
        val start_date: String?,
        val target_date: String?,
        val order: Int?,
        val active: Boolean?,
        val notes: String?,
        val isPending: Boolean = false,
        val object_group_id: Long?,
        val object_group_order: Long?,
        val object_group_title: String?
)