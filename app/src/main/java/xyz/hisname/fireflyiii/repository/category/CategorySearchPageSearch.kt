package xyz.hisname.fireflyiii.repository.category

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.data.local.dao.CategoryDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.CategoryService
import xyz.hisname.fireflyiii.repository.models.category.CategoryAttributes
import xyz.hisname.fireflyiii.repository.models.category.CategoryData

class CategorySearchPageSearch(private val searchName: String,
                               private val categoryDataDao: CategoryDataDao,
                               private val categoryService: CategoryService?): PagingSource<Int, CategoryData>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CategoryData> {
        return try {
            val networkCall = categoryService?.searchCategory(searchName)
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                responseBody.forEach { category ->
                    categoryDataDao.deleteCategoryById(category.id)
                    categoryDataDao.insert(CategoryData(category.id, CategoryAttributes("", "", category.name, "")))
                }
            }
            LoadResult.Page(categoryDataDao.searchCategory("*$searchName*"), null, null)
        } catch (exception: Exception){
            exception.printStackTrace()
            LoadResult.Page(categoryDataDao.searchCategory("*$searchName*"), null, null)
        }
    }

    override val keyReuseSupported = true
}