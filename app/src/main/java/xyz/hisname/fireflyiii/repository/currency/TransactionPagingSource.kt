package xyz.hisname.fireflyiii.repository.currency

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.util.DateTimeUtil

class TransactionPagingSource(private val currencyService: CurrencyService,
                              private val transactionDao: TransactionDataDao,
                              private val currencyCode: String,
                              private val startDate: String,
                              private val endDate: String,
                              private val transactionType: String): PagingSource<Int, Transactions>() {

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
            val networkCall = currencyService.getTransactionByCurrencyCode(currencyCode,
                    params.key ?: 1, startDate, endDate, convertString(transactionType))
            val responseBody = networkCall.body()
            if (responseBody != null && networkCall.isSuccessful) {
                if (params.key == null) {
                    transactionDao.deleteTransactionsByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                            DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), convertString(transactionType))
                }
                responseBody.data.forEach { data ->
                    data.transactionAttributes.transactions.forEach { transactions ->
                        transactionDao.insert(transactions)
                    }
                    transactionDao.insert(TransactionIndex(data.transactionId,
                            data.transactionAttributes.transactions[0].transaction_journal_id, 0))
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
                        transactionDao.getTransactionListWithCurrencyAndDate(
                                DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate),
                                convertString(transactionType), currencyCode),
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
            transactionDao.getTransactionListWithCurrencyAndDateCount(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate),convertString(transactionType), currencyCode)
        val nextKey = if(paramKey ?: 1 < (numberOfRows / Constants.PAGE_SIZE)){
            paramKey ?: 1 + 1
        } else {
            null
        }
        return LoadResult.Page(transactionDao.getTransactionListWithCurrencyAndDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate),convertString(transactionType), currencyCode),
                previousKey, nextKey)
    }

    override val keyReuseSupported = true

    private fun convertString(type: String) = type.substring(0,1).toLowerCase() + type.substring(1).toLowerCase()

}