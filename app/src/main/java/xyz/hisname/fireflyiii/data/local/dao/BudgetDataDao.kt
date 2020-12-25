package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.budget.BudgetData
import java.math.BigDecimal

@Dao
abstract class BudgetDataDao: BaseDao<BudgetData> {

    @Query("SELECT sum(amount) FROM budget WHERE (start_date =:startDate AND end_date =:endDate) AND " +
            "currency_code =:currencyCode")
    abstract fun getConstraintBudgetWithCurrency(startDate: String, endDate: String,
                                                 currencyCode: String): BigDecimal

    @Query("DELETE FROM budget")
    abstract fun deleteAllBudget(): Int
}