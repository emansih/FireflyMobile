package xyz.hisname.fireflyiii.repository.budget

import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.BudgetDataDao
import xyz.hisname.fireflyiii.data.local.dao.BudgetListDataDao
import xyz.hisname.fireflyiii.data.local.dao.SpentDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.BudgetService
import xyz.hisname.fireflyiii.repository.models.budget.BudgetData
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListModel

@Suppress("RedundantSuspendModifier")
@WorkerThread
class BudgetRepository(private val budget: BudgetDataDao,
                       private val budgetList: BudgetListDataDao,
                       private val spentDao: SpentDataDao,
                       private val budgetService: BudgetService? = null) {

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

    suspend fun allBudgetList(pageNumber: Int): MutableList<BudgetListData>{
        var networkCall: Response<BudgetListModel>?
        try {
            withContext(Dispatchers.IO) {
                networkCall = budgetService?.getPaginatedSpentBudget(pageNumber)
            }
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall?.isSuccessful == true) {
                if(pageNumber == 1){
                    withContext(Dispatchers.IO) {
                        deleteBudgetList()
                    }
                }
                withContext(Dispatchers.IO) {
                    responseBody.data.forEachIndexed { _, budgetList ->
                        insertBudgetList(budgetList)
                    }
                }

            }
        } catch (exception: Exception){ }
        return budgetList.getAllBudgetList(pageNumber = pageNumber * Constants.PAGE_SIZE)
    }

    suspend fun allActiveSpentList(currencyCode: String) = spentDao.getAllActiveBudgetList(currencyCode)

    suspend fun deleteBudgetList() = budgetList.deleteAllBudgetList()

    suspend fun retrieveConstraintBudgetWithCurrency(startDate: String, endDate: String,
                                                     currencyCode: String) =
            budget.getConstraintBudgetWithCurrency(startDate, endDate, currencyCode)

    suspend fun searchBudgetByName(budgetName: String) = budgetList.searchBudgetName(budgetName)

}