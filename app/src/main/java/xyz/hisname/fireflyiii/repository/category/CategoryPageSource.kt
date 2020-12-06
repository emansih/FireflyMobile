package xyz.hisname.fireflyiii.repository.category

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.CategoryDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.CategoryService
import xyz.hisname.fireflyiii.repository.models.category.CategoryData

class CategoryPageSource(private val categoryDataDao: CategoryDataDao,
                         private val categoryService: CategoryService?): PagingSource<Int, CategoryData>()  {


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CategoryData> {
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
            val networkCall = categoryService?.getPaginatedCategory(params.key ?: 1)
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                if (params.key == null) {
                    categoryDataDao.deleteAllCategory()
                }
                responseBody.data.forEach { data ->
                    categoryDataDao.insert(data)
                }
            }
            val pagination = responseBody?.meta?.pagination
            if(pagination != null){
                val nextKey = if(pagination.current_page < pagination.total_pages){
                    pagination.current_page + 1
                } else {
                    null
                }
                return LoadResult.Page(categoryDataDao.getCategory(), previousKey, nextKey)
            } else {
                return getOfflineData(params.key, previousKey)
            }
        } catch (exception: Exception){
            return getOfflineData(params.key, previousKey)
        }
    }

    private suspend fun getOfflineData(paramKey: Int?, previousKey: Int?): LoadResult<Int, CategoryData>{
        val numberOfRows = categoryDataDao.getCategoryCount()
        val nextKey = if(paramKey ?: 1 < (numberOfRows / Constants.PAGE_SIZE)){
            paramKey ?: 1 + 1
        } else {
            null
        }
        return LoadResult.Page(categoryDataDao.getCategory(), previousKey, nextKey)

    }

    override val keyReuseSupported = true
}