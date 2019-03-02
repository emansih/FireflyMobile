package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData

@Dao
abstract class BudgetListDataDao: BaseDao<BudgetListData>  {

    @Query("SELECT * FROM budget_list")
    abstract fun getAllBudgetList(): MutableList<BudgetListData>

    @Query("DELETE FROM budget_list")
    abstract fun deleteAllBudgetList(): Int

    @Query("SELECT * FROM budget_list WHERE name LIKE :budgetName")
    abstract fun searchBudgetName(budgetName: String): MutableList<BudgetListData>

}