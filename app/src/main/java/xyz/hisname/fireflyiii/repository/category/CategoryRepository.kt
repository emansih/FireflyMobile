package xyz.hisname.fireflyiii.repository.category

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.CategoryDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.CategoryService
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.repository.models.category.CategoryModel

@Suppress("RedundantSuspendModifier")
class CategoryRepository(private val categoryDao: CategoryDataDao,
                         private val categoryService: CategoryService?){


    suspend fun insertCategory(category: CategoryData) = categoryDao.insert(category)

    suspend fun deleteAllCategory() = categoryDao.deleteAllCategory()

    suspend fun searchCategoryByName(categoryName: String) = categoryDao.searchCategory(categoryName)


    suspend fun loadPaginatedData(pageNumber: Int): MutableList<CategoryData>{
        var networkCall: Response<CategoryModel>? = null
        try {
            withContext(Dispatchers.IO) {
                networkCall = categoryService?.getPaginatedCategory(pageNumber)
            }
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall?.isSuccessful == true) {
                if(pageNumber == 1){
                    withContext(Dispatchers.IO) {
                        deleteAllCategory()
                    }
                }
                withContext(Dispatchers.IO) {
                    responseBody.data.forEachIndexed { _, categoryData ->
                        insertCategory(categoryData)
                    }
                }

            }
        } catch (exception: Exception){ }
        return categoryDao.getPaginatedCategory(pageNumber * Constants.PAGE_SIZE)
    }
}