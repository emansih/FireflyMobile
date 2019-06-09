package xyz.hisname.fireflyiii.repository.category

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.CategoryDataDao
import xyz.hisname.fireflyiii.data.remote.api.CategoryService
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.repository.models.category.CategoryModel

@Suppress("RedundantSuspendModifier")
class CategoryRepository(private val categoryDao: CategoryDataDao,
                         private val categoryService: CategoryService?){


    suspend fun insertCategory(category: CategoryData) = categoryDao.insert(category)


    suspend fun allCategory(): MutableList<CategoryData>{
        loadRemoteData()
        return categoryDao.getAllCategory()
    }

    suspend fun deleteAllCategory() = categoryDao.deleteAllCategory()

    suspend fun searchCategoryByName(categoryName: String) = categoryDao.searchCategory(categoryName)


    private suspend fun loadRemoteData(){
        var networkCall: Response<CategoryModel>? = null
        val categoryData: MutableList<CategoryData> = arrayListOf()
        try {
            withContext(Dispatchers.IO) {
                withContext(Dispatchers.IO) {
                    networkCall = categoryService?.getPaginatedCategory(1)
                }
                categoryData.addAll(networkCall?.body()?.data?.toMutableList() ?: arrayListOf())
            }
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall?.isSuccessful != false) {
                val pagination = responseBody.meta.pagination
                if (pagination.total_pages != pagination.current_page) {
                    withContext(Dispatchers.IO) {
                        for (items in 2..pagination.total_pages) {
                            categoryData.addAll(
                                    categoryService?.getPaginatedCategory(items)
                                            ?.body()?.data?.toMutableList() ?: arrayListOf()
                            )
                        }
                    }
                }
                withContext(Dispatchers.IO) {
                    categoryDao.deleteAllCategory()
                }
                withContext(Dispatchers.IO) {
                    categoryData.forEachIndexed { _, data ->
                        insertCategory(data)
                    }
                }
            }
        } catch (exception: Exception){ }
    }
}