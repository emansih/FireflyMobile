package xyz.hisname.fireflyiii.repository.budget

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.BudgetLimitDataDao
import xyz.hisname.fireflyiii.repository.models.budget.limit.BudgetLimitData

class BudgetRepository(private val budgetDao: BudgetLimitDataDao) {

    val allBudgetLimits = budgetDao.getAllBudgetLimits()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertBudgetLimit(budgetLimit: BudgetLimitData){
        budgetDao.insert(budgetLimit)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun retrieveConstraintBudget(startDate: String, endDate: String): MutableList<BudgetLimitData>{
        return budgetDao.getConstraintBudget(startDate, endDate)
    }
}