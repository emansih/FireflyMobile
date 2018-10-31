package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.CategoryService
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.BaseResponse
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.repository.models.category.CategoryModel
import xyz.hisname.fireflyiii.util.retrofitCallback

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val categoryDatbase by lazy { AppDatabase.getInstance(application)?.categoryDataDao() }
    private var categoryService: CategoryService? = null
    private val apiLiveData: MutableLiveData<ApiResponses<CategoryModel>> = MutableLiveData()


    fun getCategory(baseUrl: String, accessToken: String): BaseResponse<CategoryData, ApiResponses<CategoryModel>> {
        val apiResponse = MediatorLiveData<ApiResponses<CategoryModel>>()
        categoryService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(CategoryService::class.java)
        categoryService?.getCategory()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                response.body()?.data?.forEachIndexed { _, element ->
                    GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT, null, {
                        categoryDatbase?.addCategory(element)
                    })
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
        return BaseResponse(categoryDatbase?.getAllCategory(), apiResponse)
    }


}