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

package xyz.hisname.fireflyiii.repository.models.budget.budgetList

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "spentList"/*, foreignKeys = [ForeignKey(entity = BudgetListData::class,
        parentColumns = arrayOf("budgetListId"), childColumns = arrayOf("spentFK"), onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE)]*/)
data class Spent(
// For some reason, foreign keys does not work. will have to settle with a hack :(
        @PrimaryKey(autoGenerate = false)
        var spentId: Long = 0,
        @Json(name ="sum")
        var amount: Double,
        var currency_code: String,
        var currency_decimal_places: Int,
        var currency_id: Int,
        var currency_symbol: String
)