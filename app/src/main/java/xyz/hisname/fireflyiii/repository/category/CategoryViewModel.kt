package xyz.hisname.fireflyiii.repository.category

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.api.CategoryService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.network.NetworkErrors
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class CategoryViewModel(application: Application): BaseViewModel(application) {

    val repository: CategoryRepository
    private val categoryService by lazy { genericService()?.create(CategoryService::class.java) }
    val categoryName =  MutableLiveData<String>()

    init {
        val categoryDataDao = AppDatabase.getInstance(application).categoryDataDao()
        repository = CategoryRepository(categoryDataDao)
    }

    fun getAllCategory(): LiveData<MutableList<CategoryData>> {
        isLoading.value = true
        var categoryData: MutableList<CategoryData> = arrayListOf()
        val data: MutableLiveData<MutableList<CategoryData>> = MutableLiveData()
        categoryService?.getCategory()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val responseBody = response.body()
                if(responseBody != null){
                    val networkData = responseBody.data
                    scope.launch(Dispatchers.IO){
                        repository.deleteAllCategory()
                    }.invokeOnCompletion {
                        categoryData.addAll(networkData)
                        if(responseBody.meta.pagination.total_pages > responseBody.meta.pagination.current_page){
                            for(items in 2..responseBody.meta.pagination.total_pages){
                                categoryService?.getPaginatedCategory(items)?.enqueue(retrofitCallback({ pagination ->
                                    pagination.body()?.data?.forEachIndexed{ _, catData ->
                                        categoryData.add(catData)
                                    }
                                }))
                            }
                        }
                        categoryData.forEachIndexed{ _, catData ->
                            scope.launch(Dispatchers.IO) {
                                repository.insertCategory(catData)
                            }
                        }
                        data.postValue(categoryData.toMutableList())
                    }
                }
            } else {
                val responseError = response.errorBody()
                if (responseError != null) {
                    val errorBody = String(responseError.bytes())
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    apiResponse.postValue(gson.message)
                }
                scope.async(Dispatchers.IO) {
                    categoryData = repository.allCategory()
                }.invokeOnCompletion {
                    data.postValue(categoryData)
                }
            }
            isLoading.value = false
        })
        { throwable ->
            scope.async(Dispatchers.IO) {
                categoryData = repository.allCategory()
            }.invokeOnCompletion {
                data.postValue(categoryData)
            }
            isLoading.value = false
            apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage))
        })
        return data
    }

    fun getCategoryByName(categoryName: String): LiveData<MutableList<CategoryData>>{
        var categoryData: MutableList<CategoryData> = arrayListOf()
        val data: MutableLiveData<MutableList<CategoryData>> = MutableLiveData()
        scope.async(Dispatchers.IO) {
            categoryData = repository.searchCategoryByName("%$categoryName%")
        }.invokeOnCompletion {
            data.postValue(categoryData)
        }
        return data
    }

    fun postCategoryName(details: String?){
        categoryName.value = details
    }
}