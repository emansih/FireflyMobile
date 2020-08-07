package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.Constants.Companion.CATEGORY_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.category.CategoryModel
import xyz.hisname.fireflyiii.repository.models.category.CategorySuccessModel


// Link to relevant doc: https://firefly-iii.readthedocs.io/en/latest/api/categories.html
interface CategoryService {

    @GET(CATEGORY_API_ENDPOINT)
    suspend fun getPaginatedCategory(@Query("page") page: Int): Response<CategoryModel>

    @FormUrlEncoded
    @POST(CATEGORY_API_ENDPOINT)
    fun createNewCategory(@Field("name") name: String): Call<CategorySuccessModel>

    @DELETE("${Constants.CATEGORY_API_ENDPOINT}/{id}")
    suspend fun deleteCategoryById(@Path("id") id: Long): Response<CategoryModel>

}