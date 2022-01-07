/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.Response
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.CATEGORY_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.category.CategoryModel
import xyz.hisname.fireflyiii.repository.models.category.CategorySuccessModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionModel


interface CategoryService {

    @GET(CATEGORY_API_ENDPOINT)
    suspend fun getPaginatedCategory(@Query("page") page: Int): Response<CategoryModel>

    @GET("${CATEGORY_API_ENDPOINT}/{id}/transactions")
    suspend fun getTransactionByCategory(@Path("id") categoryId: Long,
                                         @Query("page") page: Int,
                                         @Query("start") start: String,
                                         @Query("end") end: String,
                                         @Query("type") transactionType: String): Response<TransactionModel>

    @FormUrlEncoded
    @POST(CATEGORY_API_ENDPOINT)
    suspend fun addCategory(@Field("name") name: String): Response<CategorySuccessModel>

    @DELETE("${CATEGORY_API_ENDPOINT}/{id}")
    suspend fun deleteCategoryById(@Path("id") id: Long): Response<Void>

    @PUT("${CATEGORY_API_ENDPOINT}/{id}")
    suspend fun updateCategory(@Path("id") id: Long,
                               @Query("name") name: String): Response<CategorySuccessModel>


}