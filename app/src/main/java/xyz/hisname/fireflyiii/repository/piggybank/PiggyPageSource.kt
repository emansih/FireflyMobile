package xyz.hisname.fireflyiii.repository.piggybank

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.PiggyDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.PiggybankService
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData

class PiggyPageSource(private val piggyDao: PiggyDataDao,
                      private val piggyService: PiggybankService): PagingSource<Int, PiggyData>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PiggyData> {
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
            val networkCall = piggyService.getPaginatedPiggyBank(params.key ?: 1)
            val responseBody = networkCall.body()
            if (responseBody != null && networkCall.isSuccessful) {
                if (params.key == null) {
                    piggyDao.deleteAllPiggyBank()
                }
                responseBody.data.forEach { data ->
                    piggyDao.insert(data)
                }
            }
            val pagination = responseBody?.meta?.pagination
            if(pagination != null){
                val nextKey = if(pagination.current_page < pagination.total_pages){
                    pagination.current_page + 1
                } else {
                    null
                }
                return LoadResult.Page(piggyDao.getAllPiggy(), previousKey, nextKey)
            } else {
                return getOfflineData(params.key, previousKey)
            }
        } catch (exception: Exception){
            return getOfflineData(params.key, previousKey)
        }
    }

    private suspend fun getOfflineData(paramKey: Int?, previousKey: Int?): LoadResult<Int, PiggyData>{
        val numberOfRows = piggyDao.getAllPiggyCount()
        val nextKey = if(paramKey ?: 1 < (numberOfRows / Constants.PAGE_SIZE)){
            paramKey ?: 1 + 1
        } else {
            null
        }
        return LoadResult.Page(piggyDao.getAllPiggy(), previousKey, nextKey)

    }

    override val keyReuseSupported = true
}