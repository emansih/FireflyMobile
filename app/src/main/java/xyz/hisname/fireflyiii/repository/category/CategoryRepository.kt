package xyz.hisname.fireflyiii.repository.category

import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.CategoryDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.CategoryService
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.util.network.HttpConstants

@Suppress("RedundantSuspendModifier")
class CategoryRepository(private val categoryDao: CategoryDataDao,
                         private val categoryService: CategoryService?){


    suspend fun insertCategory(category: CategoryData) = categoryDao.insert(category)

    suspend fun deleteAllCategory() = categoryDao.deleteAllCategory()

    suspend fun searchCategoryByName(categoryName: String) = categoryDao.searchCategory(categoryName)

    suspend fun deleteCategoryById(categoryId: Long): Int{
        try {
            val networkResponse = categoryService?.deleteCategoryById(categoryId)
            when(networkResponse?.code()) {
                204 -> {
                    categoryDao.deleteCategoryById(categoryId)
                    return HttpConstants.NO_CONTENT_SUCCESS
                }
                401 -> {
                    /*   User is unauthenticated. We will retain user's data as we are
                     *   now in inconsistent state. This use case is unlikely to happen unless user
                     *   deletes their token from the web interface without updating the mobile client
                     */
                    return HttpConstants.UNAUTHORISED
                }
                404 -> {
                    // User probably deleted this on the web interface and tried to do it using mobile client
                    categoryDao.deleteCategoryById(categoryId)
                    return HttpConstants.NOT_FOUND
                }
                else -> {
                    return HttpConstants.FAILED
                }
            }
        } catch (exception: Exception){
            categoryDao.deleteCategoryById(categoryId)
            return HttpConstants.FAILED
        }
    }

    suspend fun loadPaginatedData(pageNumber: Int): Flow<MutableList<CategoryData>> {
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