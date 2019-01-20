package xyz.hisname.fireflyiii.data.remote.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import xyz.hisname.fireflyiii.Constants.Companion.CATEGORY_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.category.CategoryModel


// Link to relevant doc: https://firefly-iii.readthedocs.io/en/latest/api/categories.html
interface CategoryService {

    @GET(CATEGORY_API_ENDPOINT)
    fun getCategory(): Call<CategoryModel>

    @GET(CATEGORY_API_ENDPOINT)
    fun getPaginatedCategory(@Query("page") page: Int): Call<CategoryModel>

}