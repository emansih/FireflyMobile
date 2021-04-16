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
import androidx.room.Ignore
import com.squareup.moshi.JsonClass
import xyz.hisname.fireflyiii.repository.budget.BudgetType
import java.math.BigDecimal

@Entity
@JsonClass(generateAdapter = true)
data class BudgetListAttributes(
        var active: Boolean?,
        var created_at: String,
        var name: String,
        var order: Int?,
        @Ignore
        var spent: List<Spent> = listOf(),
        var updated_at: String,
        var auto_budget_type: BudgetType?,
        var auto_budget_currency_id: Long?,
        var auto_budget_currency_code: String?,
        var auto_budget_amount: BigDecimal?,
        var auto_budget_period: String
){
        constructor() : this(true,"","",1, listOf(),"", null,
                0, "", BigDecimal.ZERO, "")
}