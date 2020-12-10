package xyz.hisname.fireflyiii.repository.budget

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.data.local.dao.BudgetListDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.BudgetService
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListAttributes
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData

class BudgetSearchPagingSource(private val searchName: String,
                               private val budgetListDao: BudgetListDataDao,
                               private val budgetService: BudgetService): PagingSource<Int, BudgetListData>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BudgetListData> {
        return try {
            val networkCall = budgetService.searchBudget(searchName)
            val responseBody = networkCall.body()
            if (responseBody != null && networkCall.isSuccessful) {
                responseBody.forEach { budget ->
                    budgetListDao.insert(BudgetListData(budget.id,
                            BudgetListAttributes(true, "", budget.name,
                                    0, listOf(), "")))
                }
            }
            LoadResult.Page(budgetListDao.searchBudgetName("*$searchName*"), null, null)
        } catch (exception: Exception){
            LoadResult.Page(budgetListDao.searchBudgetName("*$searchName*"), null, null)
        }
    }


    override val keyReuseSupported = true

}