package xyz.hisname.fireflyiii.repository.budget

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.BudgetListDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.BudgetService
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData

class BudgetPagingSource(private val budgetDao: BudgetListDataDao,
                         private val budgetService: BudgetService?): PagingSource<Int, BudgetListData>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BudgetListData> {
        val paramKey = params.key
        val previousKey = if(paramKey != null){
            if(paramKey - 1 == 0){
                null
            } else {
                paramKey - 1
            }
        } else {
            null
        }
        try {
            val networkCall = budgetService?.getPaginatedSpentBudget(params.key ?: 1)
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                if (params.key == null) {
                    budgetDao.deleteAllBudgetList()
                }
                responseBody.data.forEach { data ->
                    budgetDao.insert(data)
                }
            }
            val pagination = responseBody?.meta?.pagination
            if(pagination != null){
                val nextKey = if(pagination.current_page < pagination.total_pages){
                    pagination.current_page + 1
                } else {
                    null
                }
                return LoadResult.Page(budgetDao.getAllBudgetList(), previousKey, nextKey)
            } else {
                return getOfflineData(params.key, previousKey)
            }
        } catch (exception: Exception){
            return getOfflineData(params.key, previousKey)
        }
    }

    private suspend fun getOfflineData(paramKey: Int?, previousKey: Int?): LoadResult<Int, BudgetListData>{
        val numberOfRows = budgetDao.getAllBudgetListCount()

        val nextKey = if(paramKey ?: 1 < (numberOfRows / Constants.PAGE_SIZE)){
            paramKey ?: 1 + 1
        } else {
            null
        }
        return LoadResult.Page(budgetDao.getAllBudgetList(), previousKey, nextKey)
    }

    override val keyReuseSupported = true
}