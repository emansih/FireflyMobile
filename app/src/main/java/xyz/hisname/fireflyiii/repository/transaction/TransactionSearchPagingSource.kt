/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.repository.transaction

import androidx.paging.PagingSource
import androidx.paging.PagingState
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
                            transactionDao.insert(TransactionIndex(0, data.transactionId,
                                    transaction.transaction_journal_id,
                                    data.transactionAttributes.group_title))
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
    override fun getRefreshKey(state: PagingState<Int, String>): Int? {
        return 1
    }

}