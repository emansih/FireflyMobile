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

package xyz.hisname.fireflyiii.repository.budget

import androidx.paging.PagingSource
import androidx.paging.PagingState
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
            val budgetId = budgetDao.searchBudgetName(budgetName)[0].budgetListId
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
    override fun getRefreshKey(state: PagingState<Int, Transactions>): Int? {
        return 1
    }

}