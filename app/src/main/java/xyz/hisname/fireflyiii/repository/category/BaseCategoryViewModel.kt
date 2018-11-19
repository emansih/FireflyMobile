package xyz.hisname.fireflyiii.repository.category

import android.app.Application
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.category.CategoryData

open class BaseCategoryViewModel(application: Application): BaseViewModel(application) {

    val repository: CategoryRepository
    var categoryData: MutableList<CategoryData>? = null

    init {
        val categoryDataDao = AppDatabase.getInstance(application).categoryDataDao()
        repository = CategoryRepository(categoryDataDao)
    }
}