package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData

@Dao
abstract class BudgetListDataDao: BaseDao<BudgetListData>  {

    @Query("SELECT * FROM budget_list WHERE active = :budgetActive ORDER BY budgetListId DESC")
    abstract suspend fun getAllBudgetList(budgetActive: Int = 1): List<BudgetListData>

    @Query("SELECT COUNT(name) FROM budget_list WHERE active = :budgetActive ORDER BY budgetListId DESC")
    abstract suspend fun getAllBudgetListCount(budgetActive: Int = 1): Long

    @Query("DELETE FROM budget_list")
    abstract suspend fun deleteAllBudgetList(): Int

    @Query("SELECT budget_list.name, budget_list.budgetListId FROM budget_list JOIN budgetListFts ON (budget_list.budgetListId = budgetListFts.budgetListId) WHERE budgetListFts MATCH :budgetName")
    abstract suspend fun searchBudgetName(budgetName: String): List<BudgetListData>

    @Query("SELECT distinct budget_list.name FROM budget_list JOIN budgetListFts ON (budget_list.budgetListId = budgetListFts.budgetListId)")
    abstract fun getAllBudgetName(): Flow<List<String>>

}