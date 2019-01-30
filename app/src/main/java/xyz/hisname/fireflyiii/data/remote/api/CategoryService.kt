package xyz.hisname.fireflyiii.data.remote.api

import retrofit2.Call
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.CATEGORY_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.category.CategoryModel
import xyz.hisname.fireflyiii.repository.models.category.CategorySuccessModel


// Link to relevant doc: https://firefly-iii.readthedocs.io/en/latest/api/categories.html
interface CategoryService {

    @GET(CATEGORY_API_ENDPOINT)
    fun getCategory(): Call<CategoryModel>

    @GET(CATEGORY_API_ENDPOINT)
    fun getPaginatedCategory(@Query("page") page: Int): Call<CategoryModel>

    @FormUrlEncoded
    @POST(CATEGORY_API_ENDPOINT)
    fun createNewCategory(@Field("name") name: String): Call<CategorySuccessModel>

}