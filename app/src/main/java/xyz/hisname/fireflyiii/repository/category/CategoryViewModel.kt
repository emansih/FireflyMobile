package xyz.hisname.fireflyiii.repository.category

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.CategoryService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.category.CategoryData

class CategoryViewModel(application: Application): BaseViewModel(application) {

    val repository: CategoryRepository
    private val categoryService by lazy { genericService()?.create(CategoryService::class.java) }

    init {
        val categoryDataDao = AppDatabase.getInstance(application).categoryDataDao()
        repository = CategoryRepository(categoryDataDao, categoryService)
    }


    fun getCategoryByName(categoryName: String): LiveData<List<CategoryData>>{
        val data: MutableLiveData<List<CategoryData>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO) {
            data.postValue(repository.searchCategoryByName(categoryName))
        }
        return data
    }

}