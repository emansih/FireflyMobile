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
import xyz.hisname.fireflyiii.repository.category.CategorySearchPageSearch
import xyz.hisname.fireflyiii.repository.models.category.CategoryData

class CategoriesDialogViewModel(application: Application): BaseViewModel(application) {

    private val categoryService = genericService()?.create(CategoryService::class.java)
    private val categoryDao = AppDatabase.getInstance(application).categoryDataDao()
    val categoryName = MutableLiveData<String>()

    // load everything on first load
    fun getCategoryList() = Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)){
        CategoryPageSource(categoryDao, categoryService)
    }.flow.cachedIn(viewModelScope).asLiveData()

    fun searchCategoryList(searchName: String) = Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)){
        CategorySearchPageSearch(searchName, categoryDao, categoryService)
    }.flow.cachedIn(viewModelScope).asLiveData()

}
