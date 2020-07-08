package xyz.hisname.fireflyiii.repository.budget

import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import xyz.hisname.fireflyiii.data.local.dao.BudgetDataDao
import xyz.hisname.fireflyiii.data.local.dao.BudgetListDataDao
import xyz.hisname.fireflyiii.data.local.dao.SpentDataDao
import xyz.hisname.fireflyiii.repository.models.budget.BudgetData
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData

@Suppress("RedundantSuspendModifier")
@WorkerThread
class BudgetRepository(private val budget: BudgetDataDao,
                       private val budgetList: BudgetListDataDao,
                       private val spentDao: SpentDataDao) {

    suspend fun insertBudget(budgetData: BudgetData){
        budget.insert(budgetData)
    }

    suspend fun deleteAllBudget() = budget.deleteAllBudget()

    suspend fun insertBudgetList(budgetData: BudgetListData){
        withContext(Dispatchers.Main){
            async(Dispatchers.IO){
                budgetList.insert(budgetData)
            }.invokeOnCompletion {
                val spentList = budgetData.budgetListAttributes?.spent
                if(spentList != null && spentList.isNotEmpty()){
                    spentList.forEach { spent ->
                        spent.spentId = budgetData.budgetListId ?: 0
                        spentDao.insert(spent)
                    }
                }
            }
        }
    }

    suspend fun allBudgetList() = budgetList.getAllBudgetList()

    suspend fun allActiveSpentList(currencyCode: String) = spentDao.getAllActiveBudgetList(currencyCode)

    suspend fun deleteBudgetList() = budgetList.deleteAllBudgetList()

    suspend fun retrieveConstraintBudgetWithCurrency(startDate: String, endDate: String,
                                                     currencyCode: String) =
            budget.getConstraintBudgetWithCurrency(startDate, endDate, currencyCode)

    suspend fun searchBudgetByName(budgetName: String) = budgetList.searchBudgetName(budgetName)

}