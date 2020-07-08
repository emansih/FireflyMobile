package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.Spent

@Dao
abstract class SpentDataDao: BaseDao<Spent>  {

    @Query("SELECT ABS(SUM(amount)) FROM budget_list INNER JOIN spentList ON spentList.spentId = budget_list.budgetListId WHERE active = 1 AND currency_code =:currencyCode")
    abstract fun getAllActiveBudgetList(currencyCode: String): Double

}