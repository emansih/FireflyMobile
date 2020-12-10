package xyz.hisname.fireflyiii.repository.currency

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.CurrencyDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData

class CurrencyPagingSource(private val currencyDataDao: CurrencyDataDao,
                           private val currencyService: CurrencyService): PagingSource<Int, CurrencyData>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CurrencyData> {
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
            val networkCall = currencyService.getPaginatedCurrency(params.key ?: 1)
            val responseBody = networkCall.body()
            if (responseBody != null && networkCall.isSuccessful) {
                if (params.key == null) {
                    currencyDataDao.deleteAllCurrency()
                }
                responseBody.data.forEach { data ->
                    currencyDataDao.insert(data)
                }
            }
            val pagination = responseBody?.meta?.pagination
            if(pagination != null){
                val nextKey = if(pagination.current_page < pagination.total_pages){
                    pagination.current_page + 1
                } else {
                    null
                }
                return LoadResult.Page(currencyDataDao.getCurrency(), previousKey, nextKey)
            } else {
                return getOfflineData(params.key, previousKey)
            }
        } catch (exception: Exception){
            return getOfflineData(params.key, previousKey)
        }
    }

    private suspend fun getOfflineData(paramKey: Int?, previousKey: Int?): LoadResult<Int, CurrencyData>{
        val numberOfRows = currencyDataDao.getCurrencyCount()
        val nextKey = if(paramKey ?: 1 < (numberOfRows / Constants.PAGE_SIZE)){
            paramKey ?: 1 + 1
        } else {
            null
        }
        return LoadResult.Page(currencyDataDao.getCurrency(), previousKey, nextKey)

    }

    override val keyReuseSupported = true

}