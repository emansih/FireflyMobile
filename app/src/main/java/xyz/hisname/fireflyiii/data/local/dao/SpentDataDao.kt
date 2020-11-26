package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.Spent
import java.math.BigDecimal

@Dao
abstract class SpentDataDao: BaseDao<Spent>  {

    @Query("SELECT ABS(SUM(amount)) FROM budget_list INNER JOIN spentList ON spentList.spentId = budget_list.budgetListId WHERE active = 1 AND currency_code =:currencyCode")
    abstract fun getAllActiveBudgetList(currencyCode: String): BigDecimal

    @Query("SELECT ABS(SUM(amount)) FROM budget_list INNER JOIN spentList ON spentList.spentId = budget_list.budgetListId WHERE active = 1 AND currency_code =:currencyCode AND budgetListId =:budgetLimitId")
    abstract fun getBudgetListByIdAndCurrencyCode(budgetLimitId: Long, currencyCode: String): BigDecimal

}