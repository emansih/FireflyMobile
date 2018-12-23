package xyz.hisname.fireflyiii.repository.budget

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.BudgetDataDao
import xyz.hisname.fireflyiii.repository.models.budget.BudgetData

class BudgetRepository(private val budget: BudgetDataDao) {

    val allBudget = budget.getAllBudget()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertBudget(budgetData: BudgetData){
        budget.insert(budgetData)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun retrieveConstraintBudgetWithCurrency(startDate: String, endDate: String,
                                                     currencyCode: String): MutableList<BudgetData>{
        return budget.getConstraintBudgetWithCurrency(startDate, endDate, currencyCode)
    }

}