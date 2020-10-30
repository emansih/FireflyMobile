package xyz.hisname.fireflyiii.repository.category

import android.app.Application
import androidx.lifecycle.*
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.CategoryService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.repository.models.category.CategorySuccessModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import xyz.hisname.fireflyiii.workers.DeleteCategoryWorker

class CategoryViewModel(application: Application): BaseViewModel(application) {

    val repository: CategoryRepository
    private val categoryService by lazy { genericService()?.create(CategoryService::class.java) }
    val categoryName =  MutableLiveData<String>()

    init {
        val categoryDataDao = AppDatabase.getInstance(application).categoryDataDao()
        repository = CategoryRepository(categoryDataDao, categoryService)
    }

    fun getPaginatedCategory(pageNumber: Int): LiveData<MutableList<CategoryData>>{
        val data: MutableLiveData<MutableList<CategoryData>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            repository.loadPaginatedData(pageNumber).collectLatest {
                data.postValue(it)
            }
        }
        return data
    }

    fun getCategoryByName(categoryName: String): LiveData<MutableList<CategoryData>>{
        var categoryData: MutableList<CategoryData> = arrayListOf()
        val data: MutableLiveData<MutableList<CategoryData>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO) {
            categoryData = repository.searchCategoryByName("*$categoryName*")
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
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorMessage)
                errorMessage = when {
                    moshi?.errors?.name != null -> moshi.errors.name[0]
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

    fun deleteCategoryByName(categoryName: String): LiveData<Boolean> {
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        var isItDeleted = 0
        var categoryId = 0L
        viewModelScope.launch(Dispatchers.IO) {
            categoryId = repository.searchCategoryByName(categoryName)[0].categoryId ?: 0
            if(categoryId != 0L){
                isItDeleted = repository.deleteCategoryById(categoryId)
            }
        }.invokeOnCompletion {
            // Since onDraw() is being called multiple times, we check if the category exists locally in the DB.
            when (isItDeleted) {
                HttpConstants.FAILED -> {
                    isDeleted.postValue(false)
                    DeleteCategoryWorker.initPeriodicWorker(categoryId, getApplication())
                }
                HttpConstants.UNAUTHORISED -> {
                    isDeleted.postValue(false)
                }
                HttpConstants.NO_CONTENT_SUCCESS -> {
                    isDeleted.postValue(true)
                }
            }
        }
        return isDeleted
    }
}