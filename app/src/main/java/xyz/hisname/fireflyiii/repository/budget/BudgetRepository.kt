package xyz.hisname.fireflyiii.repository.budget

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.BudgetDataDao
import xyz.hisname.fireflyiii.data.local.dao.BudgetLimitDao
import xyz.hisname.fireflyiii.data.local.dao.BudgetListDataDao
import xyz.hisname.fireflyiii.data.local.dao.SpentDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.BudgetService
import xyz.hisname.fireflyiii.repository.models.budget.BudgetData
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData

@Suppress("RedundantSuspendModifier")
@WorkerThread
class BudgetRepository(private val budget: BudgetDataDao,
                       private val budgetList: BudgetListDataDao,
                       private val spentDao: SpentDataDao,
                       private val budgetLimitDao: BudgetLimitDao,
                       private val budgetService: BudgetService? = null) {

    suspend fun insertBudget(budgetData: BudgetData){
        budget.insert(budgetData)
    }

    suspend fun deleteAllBudget() = budget.deleteAllBudget()

    suspend fun insertBudgetList(budgetData: BudgetListData){
        budgetList.insert(budgetData)
        val spentList = budgetData.budgetListAttributes?.spent
        if(spentList != null && spentList.isNotEmpty()){
            spentList.forEach { spent ->
                spent.spentId = budgetData.budgetListId ?: 0
                spentDao.insert(spent)
            }
        }
    }

    suspend fun allBudgetList(pageNumber: Int): MutableList<BudgetListData>{
        try {
            val networkCall = budgetService?.getPaginatedSpentBudget(pageNumber)
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                if (pageNumber == 1) {
                    deleteBudgetList()
                }
                responseBody.data.forEachIndexed { _, budgetList ->
                    insertBudgetList(budgetList)
                }
            }
        } catch (exception: Exception){ }
        return budgetList.getAllBudgetList(pageNumber = pageNumber * Constants.PAGE_SIZE)
    }

    suspend fun allActiveSpentList(currencyCode: String, startDate: String, endDate: String): Double{
        try {
            val budgetListData: MutableList<BudgetListData> = arrayListOf()
            val networkCall = budgetService?.getPaginatedSpentBudget(1, startDate, endDate)
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                budgetListData.addAll(responseBody.data)
                if (responseBody.meta.pagination.current_page == 1) {
                    deleteBudgetList()
                }
                if (responseBody.meta.pagination.current_page != responseBody.meta.pagination.total_pages) {
                    for (pagination in 2..responseBody.meta.pagination.total_pages) {
                        val repeatedCall = budgetService?.getPaginatedSpentBudget(pagination, startDate, endDate)
                        val repeatedCallBody = repeatedCall?.body()
                        if (repeatedCallBody != null) {
                            budgetListData.addAll(repeatedCallBody.data)
                        }
                    }
                }
                budgetListData.forEach { budgetList ->
                    insertBudgetList(budgetList)
                }
            }
        } catch (exception: Exception){ }
        return spentDao.getAllActiveBudgetList(currencyCode)
    }

    suspend fun deleteBudgetList() = budgetList.deleteAllBudgetList()

    suspend fun getBudgetListByIdAndCurrencyCode(budgetName: String, currencyCode: String): Double {
        val budgetNameList = searchBudgetByName(budgetName)
        val budgetId = budgetNameList[0].budgetListId ?: 0
        return spentDao.getBudgetListByIdAndCurrencyCode(budgetId, currencyCode)
    }

    suspend fun retrieveConstraintBudgetWithCurrency(startDate: String, endDate: String,
                                                     currencyCode: String) =
            budget.getConstraintBudgetWithCurrency(startDate, endDate, currencyCode)

    suspend fun searchBudgetByName(budgetName: String) = budgetList.searchBudgetName(budgetName)

    suspend fun getBudgetLimitByName(budgetName: String, currencyCode: String, startDate: String, endDate: String): Double{
        val budgetNameList = searchBudgetByName(budgetName)
        val budgetId = budgetNameList[0].budgetListId ?: 0
        try {
            val networkCall = budgetService?.getBudgetLimit(budgetId, startDate, endDate)
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                budgetLimitDao.deleteAllBudgetLimit()
                responseBody.budgetLimitData.forEach { limitData ->
                    budgetLimitDao.insert(limitData)
                }
            }
        } catch (exception: Exception){ }
        return budgetLimitDao.getBudgetLimitByIdAndCurrencyCode(budgetId, currencyCode)
    }
}