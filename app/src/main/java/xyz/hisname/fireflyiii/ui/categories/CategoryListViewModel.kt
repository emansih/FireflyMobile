package xyz.hisname.fireflyiii.ui.categories

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.CategoryService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.category.CategoryPageSource
import xyz.hisname.fireflyiii.repository.category.CategoryRepository
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.DeleteCategoryWorker

class CategoryListViewModel(application: Application): BaseViewModel(application) {

    private val categoryDao = AppDatabase.getInstance(application).categoryDataDao()
    private val categoryService = genericService()?.create(CategoryService::class.java)
    private val categoryRepository = CategoryRepository(categoryDao, categoryService)

    fun getCategories() =
        Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)){
            CategoryPageSource(categoryDao, categoryService)
        }.flow.cachedIn(viewModelScope).asLiveData()


    fun deleteCategory(categoryId: String): LiveData<Boolean> {
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO) {
            val categoryList = categoryRepository.getCategoryById(categoryId.toLong())
            if(categoryList.categoryId != null || categoryList.categoryId != 0L){
                // Since onDraw() is being called multiple times, we check if the category exists locally in the DB.
                when (categoryRepository.deleteCategoryById(categoryId.toLong())) {
                    HttpConstants.FAILED -> {
                        isDeleted.postValue(false)
                        DeleteCategoryWorker.initPeriodicWorker(categoryId.toLong(), getApplication())
                    }
                    HttpConstants.UNAUTHORISED -> {
                        isDeleted.postValue(false)
                    }
                    HttpConstants.NO_CONTENT_SUCCESS -> {
                        isDeleted.postValue(true)
                    }
                }
            }
        }
        return isDeleted
    }
}