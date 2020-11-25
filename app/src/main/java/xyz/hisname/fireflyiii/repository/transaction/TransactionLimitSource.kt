package xyz.hisname.fireflyiii.repository.transaction

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions

class TransactionLimitSource(private val limit: Int,
                             private val transactionDao: TransactionDataDao,
                             private val transactionService: TransactionService?): PagingSource<Int, Transactions>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Transactions> {
        try {
            val networkCall = transactionService?.getPaginatedTransactions("", "",
                    "all", 1)
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                responseBody.data.forEach { data ->
                    data.transactionAttributes?.transactions?.forEach { transactions ->
                        transactionDao.insert(transactions)
                    }
                    transactionDao.insert(TransactionIndex(data.transactionId,
                            data.transactionAttributes?.transactions?.get(0)?.transaction_journal_id))
                }
            }
        } catch(exception: Exception){
            exception.printStackTrace()
        }
        return LoadResult.Page(transactionDao.getTransactionLimit(limit), null, null)
    }

    override val keyReuseSupported = true

}
