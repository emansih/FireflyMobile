package xyz.hisname.fireflyiii.repository.budget

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.BudgetDataDao
import xyz.hisname.fireflyiii.data.local.dao.BudgetLimitDataDao
import xyz.hisname.fireflyiii.repository.models.budget.budget.BudgetData
import xyz.hisname.fireflyiii.repository.models.budget.limit.BudgetLimitData

class BudgetRepository(private val budgetLimitDao: BudgetLimitDataDao, private val budget: BudgetDataDao) {

    val allBudgetLimits = budgetLimitDao.getAllBudgetLimits()
    val allBudget = budget.getAllBudget()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertBudgetLimit(budgetLimit: BudgetLimitData){
        budgetLimitDao.insert(budgetLimit)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertBudget(budgetData: BudgetData){
        budget.insert(budgetData)
    }

            @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun retrieveConstraintBudget(startDate: String, endDate: String): MutableList<BudgetLimitData>{
        return budgetLimitDao.getConstraintBudget(startDate, endDate)
    }
}