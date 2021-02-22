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
import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData

@Dao
abstract class BudgetListDataDao: BaseDao<BudgetListData>  {

    @Query("DELETE FROM budget_list")
    abstract suspend fun deleteAllBudgetList(): Int

    @Query("DELETE FROM budget_list WHERE budgetListId =:budgetId")
    abstract suspend fun deleteBudgetById(budgetId: Long): Int

    @Query("SELECT budget_list.name, budget_list.budgetListId FROM budget_list JOIN budgetListFts ON (budget_list.budgetListId = budgetListFts.budgetListId) WHERE budgetListFts MATCH :budgetName")
    abstract suspend fun searchBudgetName(budgetName: String): List<BudgetListData>

    @Query("SELECT * FROM budget_list JOIN budgetListFts ON (budget_list.budgetListId = budgetListFts.budgetListId)")
    abstract fun getAllBudgetFlow(): Flow<List<BudgetListData>>

    @Query("SELECT * FROM budget_list JOIN budgetListFts ON (budget_list.budgetListId = budgetListFts.budgetListId)")
    abstract fun getAllBudget(): List<BudgetListData>

}