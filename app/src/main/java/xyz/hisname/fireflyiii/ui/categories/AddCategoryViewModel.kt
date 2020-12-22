package xyz.hisname.fireflyiii.ui.categories

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.CategoryService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.category.CategoryRepository
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.workers.category.CategoryWorker
import java.net.UnknownHostException

class AddCategoryViewModel(application: Application): BaseViewModel(application) {

    private val categoryService by lazy { genericService().create(CategoryService::class.java) }
    private val categoryDataDao = AppDatabase.getInstance(application).categoryDataDao()
    private val repository = CategoryRepository(categoryDataDao, categoryService)

    fun getCategoryById(categoryId: Long): LiveData<CategoryData>{
        val categoryLiveData: MutableLiveData<CategoryData> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            val categoryData = repository.getCategoryById(categoryId)
            categoryLiveData.postValue(categoryData)
        }
        return categoryLiveData
    }

    fun updateCategory(categoryId: Long, categoryName: String): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            val updateCategory = repository.updateCategory(categoryId, categoryName)
            when {
                updateCategory.response != null -> {
                    apiResponse.postValue(Pair(true, "$categoryName updated"))
                }
                updateCategory.errorMessage != null -> {
                    apiResponse.postValue(Pair(false, updateCategory.errorMessage))
                }
                updateCategory.error != null -> {
                    apiResponse.postValue(Pair(false, updateCategory.error.localizedMessage))
                }
                else -> {
                    apiResponse.postValue(Pair(false, "Error updating category"))
                }
            }
            isLoading.postValue(false)
        }
        return apiResponse
    }

    fun addCategory(categoryName: String): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            val addCategory = repository.addCategory(categoryName)
            when {
                addCategory.response != null -> {
                    apiResponse.postValue(Pair(true,
                            getApplication<Application>().getString(R.string.category_added, categoryName)))
                }
                addCategory.errorMessage != null -> {
                    apiResponse.postValue(Pair(false, addCategory.errorMessage))
                }
                addCategory.error != null -> {
                    if (addCategory.error is UnknownHostException) {
                        CategoryWorker.initPeriodicWorker(categoryName, getApplication())
                        apiResponse.postValue(Pair(true, getApplication<Application>().getString(R.string.data_added_when_user_online, "Category")))
                    } else {
                        apiResponse.postValue(Pair(false, addCategory.error.localizedMessage))
                    }
                }
                else -> {
                    apiResponse.postValue(Pair(false, "Error saving category"))
                }
            }
            isLoading.postValue(false)
        }
        return apiResponse
    }
}