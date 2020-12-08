package xyz.hisname.fireflyiii.repository.piggybank

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.data.local.dao.PiggyDataDao
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData

class SearchPiggyPageSource(private val piggyDao: PiggyDataDao,
                            private val searchQuery: String): PagingSource<Int, PiggyData>() {


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PiggyData> {
        return LoadResult.Page(piggyDao.searchPiggyName("*$searchQuery*"), null, null)
    }

    override val keyReuseSupported = true

}