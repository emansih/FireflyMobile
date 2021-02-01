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

package xyz.hisname.fireflyiii.repository.models.accounts

import androidx.room.Entity
import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@Entity
@JsonClass(generateAdapter = true)
data class AccountAttributes(
        val updated_at: String,
        val created_at: String,
        val name: String,
        val active: Boolean,
        val type: String,
        val account_role: String?,
        val currency_id: Long?,
        val currency_code: String?,
        val current_balance: BigDecimal,
        val currency_symbol: String?,
        val current_balance_date: String,
        val notes: String?,
        val monthly_payment_date: String?,
        val credit_card_type: String?,
        val account_number: String?,
        val iban: String?,
        val bic: String?,
        val virtual_balance: Double?,
        val opening_balance: BigDecimal?,
        val opening_balance_date: String?,
        val liability_type: String?,
        val liability_amount: String?,
        val liability_start_date: String?,
        val interest: String?,
        val interest_period: String?,
        val include_net_worth: Boolean,
        val isPending: Boolean = false
)