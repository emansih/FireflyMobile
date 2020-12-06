package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.AUTOCOMPLETE_API_ENDPOINT
import xyz.hisname.fireflyiii.Constants.Companion.CATEGORY_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.autocomplete.CategoriesItems
import xyz.hisname.fireflyiii.repository.models.category.CategoryModel
import xyz.hisname.fireflyiii.repository.models.category.CategorySuccessModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionModel


interface CategoryService {

    @GET(CATEGORY_API_ENDPOINT)
    suspend fun getPaginatedCategory(@Query("page") page: Int): Response<CategoryModel>

    @GET("${AUTOCOMPLETE_API_ENDPOINT}/categories")
    suspend fun searchCategory(@Query("query") queryString: String): Response<List<CategoriesItems>>

    @GET("${CATEGORY_API_ENDPOINT}/{id}/transactions")
    suspend fun getTransactionByCategory(@Path("id") categoryId: Long,
                                         @Query("page") page: Int,
                                         @Query("start") start: String,
                                         @Query("end") end: String,
                                         @Query("type") transactionType: String): Response<TransactionModel>

    @FormUrlEncoded
    @POST(CATEGORY_API_ENDPOINT)
    fun createNewCategory(@Field("name") name: String): Call<CategorySuccessModel>

    @DELETE("${CATEGORY_API_ENDPOINT}/{id}")
    suspend fun deleteCategoryById(@Path("id") id: Long): Response<Void>

    @GET("${CATEGORY_API_ENDPOINT}/{id}")
    suspend fun updateCategory(@Path("id") id: Long,
                               @Query("name") name: String): Response<CategorySuccessModel>


}