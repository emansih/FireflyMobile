package xyz.hisname.fireflyiii.repository.category

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions

class TransactionPagingSource(private val startDate: String,
                              private val endDate: String,
                              private val categoryId: Long,
                              private val transactionDao: TransactionDataDao): PagingSource<Int, Transactions>() {


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
        val numberOfRows =
                transactionDao.getTransactionByDateAndCategoryCount(startDate, endDate, categoryId)
        val nextKey = if(paramKey ?: 1 < (numberOfRows / Constants.PAGE_SIZE)){
            paramKey ?: 1 + 1
        } else {
            null
        }
        return LoadResult.Page(transactionDao.getTransactionByDateAndCategory(startDate, endDate, categoryId), previousKey, nextKey)
    }

    override val keyReuseSupported = true
}