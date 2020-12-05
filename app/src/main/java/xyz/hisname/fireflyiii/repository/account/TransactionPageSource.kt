package xyz.hisname.fireflyiii.repository.account

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.util.DateTimeUtil

class TransactionPageSource(private val transactionDataDao: TransactionDataDao,
                            private val accountId: Long,
                            private val accountType: String,
                            private val startDate: String,
                            private val endDate: String): PagingSource<Int, Transactions>() {

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
        val numberOfRows = if(accountType.contentEquals("asset") || accountType.contentEquals("revenue")){
            transactionDataDao.getTransactionBySourceIdAndDateCount(accountId,
                    DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate))
        } else {
            transactionDataDao.getTransactionByDestinationIdAndDateCount(accountId,
                    DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate))
        }
        val nextKey = if(paramKey ?: 1 < (numberOfRows / Constants.PAGE_SIZE)){
            paramKey ?: 1 + 1
        } else {
            null
        }
        return if(accountType.contentEquals("asset") || accountType.contentEquals("revenue")){
            LoadResult.Page(transactionDataDao.getTransactionBySourceIdAndDate(accountId,
                    DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate)),
                    previousKey, nextKey)
        } else {
            LoadResult.Page(transactionDataDao.getTransactionByDestinationIdAndDate(accountId,
                    DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate)),
                    previousKey, nextKey)
        }
    }
}