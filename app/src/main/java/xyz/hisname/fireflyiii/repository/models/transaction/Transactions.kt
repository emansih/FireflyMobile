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

package xyz.hisname.fireflyiii.repository.models.transaction

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.OffsetDateTime

@JsonClass(generateAdapter = true)
@Entity(tableName = "transactionTable")
data class Transactions(
        @PrimaryKey(autoGenerate = false)
        val transaction_journal_id: Long,
        val amount: Double,
        val budget_id: Long?,
        val budget_name: String?,
        val category_id: Long?,
        val category_name: String?,
        val currency_code: String,
        val currency_decimal_places: Int,
        val currency_id: Long,
        val currency_name: String,
        val currency_symbol: String,
        val date: OffsetDateTime,
        val description: String,
        val destination_id: Long,
        val destination_name: String,
        val destination_type: String,
        val bill_id: Long?,
        val bill_name: String?,
        val due_date: String?,
        val foreign_amount: Double?,
        val foreign_currency_code: String?,
        val foreign_currency_decimal_places: String?,
        val foreign_currency_id: Long?,
        val foreign_currency_symbol: String?,
        val notes: String?,
        val order: Int,
        val source_iban: String?,
        var source_id: Long?,
        val source_name: String?,
        val source_type: String?,
        val internal_reference: String?,
        val tags: List<String>,
        @Json(name ="type")
        val transactionType: String,
        val user: Int,
        val piggy_bank_name: String?,
        val isPending: Boolean = false,
        // Don't use Uri here
        val attachment: List<String>?
)