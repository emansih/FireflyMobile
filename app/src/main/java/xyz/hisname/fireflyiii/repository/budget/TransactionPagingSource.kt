package xyz.hisname.fireflyiii.repository.budget

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.BudgetListDataDao
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.BudgetService
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.util.DateTimeUtil

class TransactionPagingSource(private val budgetService: BudgetService,
                              private val transactionDao: TransactionDataDao,
                              private val budgetDao: BudgetListDataDao,
                              private val budgetName: String,
                              private val startDate: String,
                              private val endDate: String,
                              private val currencyCode: String): PagingSource<Int, Transactions>() {


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Transactions> {
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
            val budgetId = budgetDao.searchBudgetName(budgetName)[0].budgetListId ?: 0
            val networkCall = budgetService.getPaginatedTransactionByBudget(budgetId,
                    params.key ?: 1, startDate, endDate, "all")
            val responseBody = networkCall.body()
            if (responseBody != null && networkCall.isSuccessful) {
                if (params.key == null) {
                    transactionDao.deleteTransactionsByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                            DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate))
                }
                responseBody.data.forEach { data ->
                    data.transactionAttributes.transactions.forEach { transactions ->
                        transactionDao.insert(transactions)
                        transactionDao.insert(TransactionIndex(0, data.transactionId,
                                transactions.transaction_journal_id,
                                data.transactionAttributes.group_title))
                    }
                }
            }
            val pagination = responseBody?.meta?.pagination
            if(pagination != null){
                val nextKey = if(pagination.current_page < pagination.total_pages){
                    pagination.current_page + 1
                } else {
                    null
                }
                return LoadResult.Page(
                        transactionDao.getTransactionListByDateAndBudget(
                                DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate),
                                budgetName, currencyCode),
                        previousKey, nextKey)
            } else {
                return getOfflineData(params.key, previousKey)
            }
        } catch (exception: Exception){
            return getOfflineData(params.key, previousKey)
        }
    }


    private suspend fun getOfflineData(paramKey: Int?, previousKey: Int?): LoadResult<Int, Transactions>{
        val numberOfRows =
                transactionDao.getTransactionByDateCount(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                        DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate))

        val nextKey = if(paramKey ?: 1 < (numberOfRows / Constants.PAGE_SIZE)){
            paramKey ?: 1 + 1
        } else {
            null
        }
        return LoadResult.Page(transactionDao.getTransactionListByDateAndBudget(
                DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate),budgetName, currencyCode),
                previousKey, nextKey)
    }

    override val keyReuseSupported = true

}