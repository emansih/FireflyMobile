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

package xyz.hisname.fireflyiii.repository.account

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.AccountsDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData

class AccountSearchPageSource(private val query: String,
                              private val accountType: String,
                              private val accountDao: AccountsDataDao,
                              private val accountService: AccountsService): PagingSource<Int, AccountData>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AccountData> {
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
            val networkCall = accountService.searchAccount(query, accountType)
            val responseBody = networkCall.body()
            if (responseBody != null && networkCall.isSuccessful) {
                responseBody.data.forEach { account ->
                    accountDao.insert(account)
                }
            }
            val pagination = responseBody?.meta?.pagination
            if(pagination != null){
                val nextKey = if(pagination.current_page < pagination.total_pages){
                    pagination.current_page + 1
                } else {
                    null
                }
                return LoadResult.Page(accountDao.searchAccountDataByNameAndType(accountType, "%$query%"), previousKey, nextKey)
            } else {
                return getOfflineData(params.key, previousKey)
            }
        } catch (exception: Exception){
            return getOfflineData(params.key, previousKey)
        }
    }

    private suspend fun getOfflineData(paramKey: Int?, previousKey: Int?): LoadResult<Int, AccountData>{
        val numberOfRows = accountDao.searchAccountDataByNameAndTypeCount(accountType, "%$query%")
        val nextKey = if(paramKey ?: 1 < (numberOfRows / Constants.PAGE_SIZE)){
            paramKey ?: 1 + 1
        } else {
            null
        }
        return LoadResult.Page(accountDao.searchAccountDataByNameAndType(accountType, "%$query%"), previousKey, nextKey)
    }

    override val keyReuseSupported = true
}