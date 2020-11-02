package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.budget.limits.BudgetLimitData
import java.math.BigDecimal

@Dao
abstract class BudgetLimitDao: BaseDao<BudgetLimitData> {

    @Query("DELETE FROM budgetLimit")
    abstract fun deleteAllBudgetLimit(): Int

    @Query("SELECT amount FROM budgetlimit WHERE budget_id =:budgetId AND currency_code =:currencyCode AND start =:startDate AND `end` =:endDate")
    abstract fun getBudgetLimitByIdAndCurrencyCodeAndDate(budgetId: Long, currencyCode: String,
                                                          startDate: String, endDate: String): BigDecimal

}