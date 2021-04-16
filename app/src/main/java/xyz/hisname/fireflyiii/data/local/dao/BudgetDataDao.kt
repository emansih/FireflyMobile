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
import xyz.hisname.fireflyiii.repository.models.budget.BudgetData
import java.math.BigDecimal

@Dao
abstract class BudgetDataDao: BaseDao<BudgetData> {

    // For some reason this doesn't work... It is returning NULL
/*
    @Query("SELECT sum(amount) FROM budget WHERE (start_date =:startDate AND end_date =:endDate) AND currency_code =:currencyCode")
    abstract fun getConstraintBudgetWithCurrency(startDate: String, endDate: String,
                                                 currencyCode: String): BigDecimal
*/

    @Query("SELECT * FROM budget WHERE (start_date =:startDate AND end_date =:endDate) AND currency_code =:currencyCode")
    abstract fun getBudgetWithCurrency(startDate: String, endDate: String,
                                       currencyCode: String): List<BudgetData>


    @Query("DELETE FROM budget")
    abstract fun deleteAllBudget(): Int

    @Query("SELECT * FROM budget WHERE (start_date =:startDate AND end_date =:endDate) AND currency_code =:currencyCode")
    abstract fun getBudgetByCurrencyAndStartEndDate(startDate: String, endDate: String,
                                                    currencyCode: String): BudgetData
}