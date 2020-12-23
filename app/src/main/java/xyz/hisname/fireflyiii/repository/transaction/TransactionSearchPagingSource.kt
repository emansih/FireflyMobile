package xyz.hisname.fireflyiii.repository.transaction

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex

class TransactionSearchPagingSource(private val transactionService: TransactionService,
                                    private val transactionDao: TransactionDataDao,
                                    private val query: String): PagingSource<Int, String>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> {
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
        if(query.isBlank()){
            return LoadResult.Page(transactionDao.getTransactionByDescription(), null, null)
        } else {
            try {
                val networkCall = transactionService.searchTransaction(query)
                val responseBody = networkCall.body()
                if (responseBody != null && networkCall.isSuccessful) {
                    responseBody.data.forEach { data ->
                        data.transactionAttributes.transactions.forEach { transaction ->
                            transactionDao.insert(transaction)
                            transactionDao.insert(TransactionIndex(data.transactionId, transaction.transaction_journal_id, 0))
                        }
                    }
                }
                val pagination = responseBody?.meta?.pagination
                return if(pagination != null){
                    val nextKey = if(pagination.current_page < pagination.total_pages){
                        pagination.current_page + 1
                    } else {
                        null
                    }
                    LoadResult.Page(transactionDao.getTransactionByDescription("%$query%"),
                            previousKey, nextKey)
                } else {
                    getOfflineData(params.key, previousKey)
                }
            } catch (exception: Exception){
                return getOfflineData(params.key, previousKey)
            }
        }
    }


    private suspend fun getOfflineData(paramKey: Int?, previousKey: Int?): LoadResult<Int, String>{
        val numberOfRows = transactionDao.getTransactionByDescriptionCount("%$query%")
        val nextKey = if(paramKey ?: 1 < (numberOfRows / Constants.PAGE_SIZE)){
            paramKey ?: 1 + 1
        } else {
            null
        }
        return LoadResult.Page(transactionDao.getTransactionByDescription("%$query%"),
                previousKey, nextKey)

    }

    override val keyReuseSupported = true

}