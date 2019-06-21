package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData

@Dao
abstract class BudgetListDataDao: BaseDao<BudgetListData>  {

    @Query("SELECT * FROM budget_list WHERE active = :budgetActive")
    abstract fun getAllBudgetList(budgetActive: Boolean = true): MutableList<BudgetListData>

    @Query("DELETE FROM budget_list")
    abstract fun deleteAllBudgetList(): Int

    @Transaction
    @Query("SELECT budget_list.name FROM budget_list JOIN budgetListFts ON (budget_list.budgetListId " +
            "= budgetListFts.budgetListId) WHERE budgetListFts MATCH :budgetName")
    abstract fun searchBudgetName(budgetName: String): MutableList<BudgetListData>
}