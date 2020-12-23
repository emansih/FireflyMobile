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