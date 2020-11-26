package xyz.hisname.fireflyiii.repository.transaction

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions

class TransactionLimitSource(private val limit: Int,
                             private val transactionDao: TransactionDataDao): PagingSource<Int, Transactions>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Transactions> {
        return LoadResult.Page(transactionDao.getTransactionLimit(limit), null, null)
    }

    override val keyReuseSupported = true

}
