package xyz.hisname.fireflyiii.repository.category

import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.CategoryDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.CategoryService
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.workers.account.DeleteAccountWorker

@Suppress("RedundantSuspendModifier")
class CategoryRepository(private val categoryDao: CategoryDataDao,
                         private val categoryService: CategoryService?){


    suspend fun insertCategory(category: CategoryData) = categoryDao.insert(category)

    suspend fun deleteAllCategory() = categoryDao.deleteAllCategory()

    suspend fun searchCategoryByName(categoryName: String) = categoryDao.searchCategory(categoryName)

    suspend fun deleteCategoryById(categoryId: Long): Boolean{
        var isDeleted = false
        try {
            val networkResponse = categoryService?.deleteCategoryById(categoryId)
            isDeleted = if (networkResponse?.code() == 204 || networkResponse?.code() == 200) {
                categoryDao.deleteCategoryById(categoryId)
                true
            } else {
                false
            }
        } catch (exception: Exception){ }
        return isDeleted
    }

    suspend fun loadPaginatedData(pageNumber: Int): MutableList<CategoryData>{
        try {
            val networkCall = categoryService?.getPaginatedCategory(pageNumber)
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                if (pageNumber == 1) {
                    deleteAllCategory()
                }
                responseBody.data.forEachIndexed { _, categoryData ->
                    insertCategory(categoryData)
                }
            }
        } catch (exception: Exception){ }
        return categoryDao.getPaginatedCategory(pageNumber * Constants.PAGE_SIZE)
    }
}