package xyz.hisname.fireflyiii.repository.budget

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.BudgetDataDao
import xyz.hisname.fireflyiii.data.local.dao.BudgetListDataDao
import xyz.hisname.fireflyiii.repository.models.budget.BudgetData
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData

class BudgetRepository(private val budget: BudgetDataDao, private val budgetList: BudgetListDataDao) {

    val allBudget = budget.getAllBudget()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertBudget(budgetData: BudgetData){
        budget.insert(budgetData)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAllBudget() = budget.deleteAllBudget()


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertBudgetList(budgetData: BudgetListData) = budgetList.insert(budgetData)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun allBudgetList(): MutableList<BudgetListData>{
         return budgetList.getAllBudgetList()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteBudgetList() = budgetList.deleteAllBudgetList()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun retrieveConstraintBudgetWithCurrency(startDate: String, endDate: String,
                                                     currencyCode: String): MutableList<BudgetData>{
        return budget.getConstraintBudgetWithCurrency(startDate, endDate, currencyCode)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun searchBudgetByName(budgetName: String) = budgetList.searchBudgetName(budgetName)

}