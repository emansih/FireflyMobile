package xyz.hisname.fireflyiii.repository.category

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.CategoryDataDao
import xyz.hisname.fireflyiii.repository.models.category.CategoryData

class CategoryRepository(private val categoryDao: CategoryDataDao){


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertCategory(category: CategoryData){
        categoryDao.insert(category)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun allCategory() = categoryDao.getAllCategory()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAllCategory() = categoryDao.deleteAllCategory()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun searchCategoryByName(categoryName: String) = categoryDao.searchCategory(categoryName)
}