package xyz.hisname.fireflyiii.repository.category

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.CategoryDataDao
import xyz.hisname.fireflyiii.repository.models.category.CategoryData

class CategoryRepository(private val categoryDao: CategoryDataDao){

    val allCategory = categoryDao.getAllCategory()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertCategory(category: CategoryData){
        categoryDao.insert(category)
    }


}