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
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.Constants.Companion.TRANSACTION_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionSuccessModel

interface TransactionService {

    @GET(TRANSACTION_API_ENDPOINT)
    suspend fun getPaginatedTransactions(@Query("start") startDate: String?,
                                         @Query("end") endDate: String?,
                                         @Query("type") type: String,
                                         @Query("page") page: Int): Response<TransactionModel>

    @DELETE("$TRANSACTION_API_ENDPOINT/{transactionId}")
    suspend fun deleteTransactionById(@Path("transactionId") transactionId: Long): Response<TransactionSuccessModel>

    @FormUrlEncoded
    @POST(TRANSACTION_API_ENDPOINT)
    suspend fun addSplitTransaction(@Field("group_title") groupTitle: String,
                                    @QueryMap transactionData: Map<String, String?>): Response<TransactionSuccessModel>

    @FormUrlEncoded
    @PUT("$TRANSACTION_API_ENDPOINT/{transactionId}")
    suspend fun updateTransaction(@Path("transactionId") transactionId: Long,
                                  @Field("group_title") groupTitle: String,
                                  @QueryMap transactionData: Map<String, String?>): Response<TransactionSuccessModel>


    @Deprecated("Switch to split transaction")
    @FormUrlEncoded
    @POST(TRANSACTION_API_ENDPOINT)
    suspend fun addTransaction(@Field("transactions[0][type]") type: String,
                               @Field("transactions[0][description]") description: String,
                               @Field("transactions[0][date]") date: String,
                               @Field("transactions[0][piggy_bank_name]") piggyBankName: String?,
                               @Field("transactions[0][amount]") amount: String,
                               @Field("transactions[0][source_name]") sourceName: String?,
                               @Field("transactions[0][destination_name]") destinationName: String?,
                               @Field("transactions[0][currency_code]") currency: String,
                               @Field("transactions[0][category_name]") category: String?,
                               @Field("transactions[0][tags]") tags: String?,
                               @Field("transactions[0][budget_name]") budgetName: String?,
                               @Field("transactions[0][bill_name]") billName: String?,
                               @Field("transactions[0][notes]") notes: String?): Response<TransactionSuccessModel>

    @FormUrlEncoded
    @PUT("$TRANSACTION_API_ENDPOINT/{transactionId}")
    suspend fun updateTransaction(@Path("transactionId") transactionId: Long,
                                  @Field("transactions[0][type]") type: String,
                                  @Field("transactions[0][description]") description: String,
                                  @Field("transactions[0][date]") date: String,
                                  @Field("transactions[0][piggy_bank_name]") piggyBankName: String?,
                                  @Field("transactions[0][amount]") amount: String,
                                  @Field("transactions[0][source_name]") sourceName: String?,
                                  @Field("transactions[0][destination_name]") destinationName: String?,
                                  @Field("transactions[0][currency_code]") currency: String,
                                  @Field("transactions[0][category_name]") category: String?,
                                  @Field("transactions[0][tags]") tags: String?,
                                  @Field("transactions[0][budget_name]") budgetName: String?,
                                  @Field("transactions[0][bill_name]") billName: String?,
                                  @Field("transactions[0][notes]") notes: String?): Response<TransactionSuccessModel>

    @GET("$TRANSACTION_API_ENDPOINT/{id}/attachments")
    suspend fun getTransactionAttachment(@Path("id") transactionId: Long): Response<AttachmentModel>

    @GET("${Constants.SEARCH_API_ENDPOINT}/transactions")
    suspend fun searchTransaction(@Query("query") transaction: String): Response<TransactionModel>

}