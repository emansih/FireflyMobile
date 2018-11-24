package xyz.hisname.fireflyiii.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.budget.limit.BudgetLimitData

@Dao
abstract class BudgetLimitDataDao: BaseDao<BudgetLimitData> {

    @Query("SELECT * FROM budget_limit")
    abstract fun getAllBudgetLimits(): LiveData<MutableList<BudgetLimitData>>

    @Query("SELECT * FROM budget_limit WHERE (start_date =:startDate AND end_date =:endDate)")
    abstract fun getConstraintBudget(startDate: String, endDate: String): MutableList<BudgetLimitData>
}