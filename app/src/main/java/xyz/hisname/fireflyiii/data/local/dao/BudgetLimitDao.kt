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
import xyz.hisname.fireflyiii.repository.models.budget.limits.BudgetLimitData
import java.math.BigDecimal

@Dao
abstract class BudgetLimitDao: BaseDao<BudgetLimitData> {

    @Query("DELETE FROM budgetLimit")
    abstract fun deleteAllBudgetLimit(): Int

    @Query("SELECT amount FROM budgetlimit WHERE budget_id =:budgetId AND currency_symbol =:currencySymbol AND start =:startDate AND `end` =:endDate")
    abstract fun getBudgetLimitByIdAndCurrencyCodeAndDate(budgetId: Long, currencySymbol: String,
                                                          startDate: String, endDate: String): BigDecimal

    @Query("SELECT DISTINCT(currency_symbol) FROM budgetLimit WHERE budget_Id =:budgetId")
    abstract fun getUniqueCurrencySymbolInSpentByBudgetId(budgetId: Long): List<String>

    @Query("SELECT * FROM budgetLimit WHERE budget_id =:budgetId AND currency_symbol =:currencySymbol")
    abstract fun getBudgetLimitById(budgetId: Long, currencySymbol: String): BudgetLimitData
}