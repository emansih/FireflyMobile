package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder
import xyz.hisname.fireflyiii.data.remote.api.CategoryService
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.BaseResponse
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.repository.models.category.CategoryModel
import xyz.hisname.fireflyiii.util.retrofitCallback

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val categoryDatabase by lazy { AppDatabase.getInstance(application).categoryDataDao() }
    private var categoryService: CategoryService? = null
    private val apiLiveData: MutableLiveData<ApiResponses<CategoryModel>> = MutableLiveData()
    private lateinit var localData: MutableCollection<CategoryData>


    fun getCategory(baseUrl: String, accessToken: String): BaseResponse<CategoryData, ApiResponses<CategoryModel>> {
        val apiResponse = MediatorLiveData<ApiResponses<CategoryModel>>()
        val localArray = arrayListOf<Long>()
        val networkArray = arrayListOf<Long>()
        categoryService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(CategoryService::class.java)
        categoryService?.getCategory()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val networkData = response.body()?.data
                networkData?.forEachIndexed { _, element ->
                    runBlocking(Dispatchers.IO) {
                        async(Dispatchers.IO) {
                            categoryDatabase.insert(element)
                            localData = categoryDatabase.getCategories()
                        }.await()
                        networkData.forEachIndexed { _, data ->
                            networkArray.add(data.categoryId!!)
                        }
                        localData.forEachIndexed { _, categoryData ->
                            localArray.add(categoryData.categoryId!!)
                        }
                        for (items in networkArray) {
                            localArray.remove(items)
                        }
                    }
                }
                GlobalScope.launch(Dispatchers.IO) {
                    localArray.forEachIndexed { _, categoryIndex ->
                        categoryDatabase.deleteCategoryById(categoryIndex)
                    }
                }
            }else {
                var errorBody = ""
                if (response.errorBody() != null) {
                    errorBody = String(response.errorBody()?.bytes()!!)
                }
                apiLiveData.postValue(ApiResponses(errorBody))
            }
        })
        { throwable ->  apiResponse.postValue(ApiResponses(throwable))})
        apiResponse.addSource(apiLiveData) {
            apiResponse.value = it
        }
        return BaseResponse(categoryDatabase.getAllCategory(), apiResponse)
    }


}