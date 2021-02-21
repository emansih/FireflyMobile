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

package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.Spent
import java.math.BigDecimal

@Dao
abstract class SpentDataDao: BaseDao<Spent>  {

    @Query("SELECT ABS(SUM(amount)) FROM budget_list INNER JOIN spentList ON spentList.budgetId = budget_list.budgetListId WHERE active = 1 AND currency_code =:currencyCode")
    abstract fun getAllActiveBudgetList(currencyCode: String): BigDecimal

    @Query("SELECT ABS(SUM(amount)) FROM budget_list INNER JOIN spentList ON spentList.budgetId = budget_list.budgetListId WHERE active = 1 AND currency_code =:currencyCode AND budgetListId =:budgetLimitId")
    abstract fun getBudgetListByIdAndCurrencyCode(budgetLimitId: Long, currencyCode: String): BigDecimal

    @Query("SELECT ABS(amount) FROM budget_list INNER JOIN spentList ON spentList.budgetId = budget_list.budgetListId WHERE name=:budgetName AND currency_symbol =:currencySymbol")
    abstract fun getSpentAmountByBudgetName(budgetName: String, currencySymbol: String): BigDecimal?

}