package xyz.hisname.fireflyiii.repository.category

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.api.CategoryService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.repository.models.category.CategorySuccessModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class CategoryViewModel(application: Application): BaseViewModel(application) {

    val repository: CategoryRepository
    private val categoryService by lazy { genericService()?.create(CategoryService::class.java) }
    val categoryName =  MutableLiveData<String>()

    init {
        val categoryDataDao = AppDatabase.getInstance(application).categoryDataDao()
        repository = CategoryRepository(categoryDataDao, categoryService)
    }

    fun getAllCategory(): LiveData<MutableList<CategoryData>> {
        var categoryData: MutableList<CategoryData> = arrayListOf()
        val data: MutableLiveData<MutableList<CategoryData>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            categoryData = repository.allCategory()
        }.invokeOnCompletion {
            data.postValue(categoryData)
        }
        return data
    }

    fun getCategoryByName(categoryName: String): LiveData<MutableList<CategoryData>>{
        var categoryData: MutableList<CategoryData> = arrayListOf()
        val data: MutableLiveData<MutableList<CategoryData>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO) {
            categoryData = repository.searchCategoryByName("$categoryName*")
        }.invokeOnCompletion {
            data.postValue(categoryData)
        }
        return data
    }

    fun postCategoryName(details: String?){
        categoryName.value = details
    }

    fun addCategory(categoryName: String): LiveData<ApiResponses<CategorySuccessModel>>{
        val apiResponse: MediatorLiveData<ApiResponses<CategorySuccessModel>> =  MediatorLiveData()
        val apiLiveData: MutableLiveData<ApiResponses<CategorySuccessModel>> = MutableLiveData()
        categoryService?.createNewCategory(categoryName)?.enqueue(retrofitCallback({ response ->
            var errorMessage = ""
            val responseErrorBody = response.errorBody()
            if (responseErrorBody != null) {
                errorMessage = String(responseErrorBody.bytes())
                val gson = Gson().fromJson(errorMessage, ErrorModel::class.java)
                errorMessage = when {
                    gson.errors.name != null -> gson.errors.name[0]
                    else -> "Error occurred while saving category"
                }
            }
            val networkData = response.body()
            if (networkData != null) {
                viewModelScope.launch(Dispatchers.IO) { repository.insertCategory(networkData.data) }
                apiLiveData.postValue(ApiResponses(response.body()))
            } else {
                apiLiveData.postValue(ApiResponses(errorMessage))
            }
        })
        { throwable ->
            apiResponse.postValue(ApiResponses(throwable))
        })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse
    }
}