package xyz.hisname.fireflyiii.ui.categories

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.CategoryService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.category.CategoryRepository
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.repository.models.category.CategorySuccessModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class AddCategoryViewModel(application: Application): BaseViewModel(application) {

    private val categoryService by lazy { genericService()?.create(CategoryService::class.java) }
    private val categoryDataDao = AppDatabase.getInstance(application).categoryDataDao()
    private val repository = CategoryRepository(categoryDataDao, categoryService)
    private val response: MediatorLiveData<ApiResponses<CategorySuccessModel>> =  MediatorLiveData()
    private val apiLiveData: MutableLiveData<ApiResponses<CategorySuccessModel>> = MutableLiveData()

    fun getCategoryById(categoryId: Long): LiveData<CategoryData>{
        val categoryLiveData: MutableLiveData<CategoryData> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            val categoryData = repository.getCategoryById(categoryId)
            categoryLiveData.postValue(categoryData)
        }
        return categoryLiveData
    }

    fun updateCategory(categoryId: Long, categoryName: String): LiveData<ApiResponses<CategorySuccessModel>>{
        viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            apiLiveData.postValue(ApiResponses(throwable))
        }){
            apiLiveData.postValue(ApiResponses(repository.updateCategory(categoryId, categoryName)?.body()))
        }
        response.addSource(apiLiveData){ response.value = it }
        return response
    }

    fun addCategory(categoryName: String): LiveData<ApiResponses<CategorySuccessModel>> {
        categoryService?.createNewCategory(categoryName)?.enqueue(retrofitCallback({ response ->
            var errorMessage = ""
            val responseErrorBody = response.errorBody()
            if (responseErrorBody != null) {
                errorMessage = String(responseErrorBody.bytes())
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorMessage)
                errorMessage = when {
                    moshi?.errors?.name != null -> moshi.errors.name[0]
                    else -> "Error occurred while saving category"
                }
            }
            val networkData = response.body()
            if (networkData != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.insertCategory(networkData.data)
                }
                apiLiveData.postValue(ApiResponses(response.body()))
            } else {
                apiLiveData.postValue(ApiResponses(errorMessage))
            }
        })
        { throwable ->
            response.postValue(ApiResponses(throwable))
        })
        response.addSource(apiLiveData){ response.value = it }
        return response
    }
}