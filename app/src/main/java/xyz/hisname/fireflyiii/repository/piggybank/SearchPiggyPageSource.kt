package xyz.hisname.fireflyiii.repository.piggybank

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.data.local.dao.PiggyDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.PiggybankService
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyAttributes
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData

class SearchPiggyPageSource(private val piggyDao: PiggyDataDao,
                            private val searchQuery: String,
                            private val piggybankService: PiggybankService): PagingSource<Int, PiggyData>() {


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PiggyData> {
        return try {
            val networkCall = piggybankService.searchPiggybank(searchQuery)
            val responseBody = networkCall.body()
            if (responseBody != null && networkCall.isSuccessful) {
                responseBody.forEach { piggy ->
                    piggyDao.insert(PiggyData(piggy.id,
                            PiggyAttributes("", "", piggy.name,
                                    null, null, piggy.currency_id,
                                    piggy.currency_code, piggy.currency_symbol, piggy.currency_decimal_places,
                            null, null, null, null,
                                    null, null, null, null, null, null)))
                }
            }
            LoadResult.Page(piggyDao.searchPiggyName("*$searchQuery*"), null, null)
        } catch (exception: Exception){
            LoadResult.Page(piggyDao.searchPiggyName("*$searchQuery*"), null, null)
        }
    }

    override val keyReuseSupported = true

}