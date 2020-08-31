package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.TRANSACTION_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionSuccessModel

interface TransactionService {

    @GET(TRANSACTION_API_ENDPOINT)
    suspend fun getPaginatedTransactions(@Query("start") startDate: String?,
                           @Query("end") endDate: String?,
                           @Query("type") type: String,@Query("page") page: Int): Response<TransactionModel>

    @GET("$TRANSACTION_API_ENDPOINT/{id}")
    suspend fun getTransactionById(@Path("id") id: Long): Response<TransactionModel>

    @DELETE("$TRANSACTION_API_ENDPOINT/{transactionId}")
    suspend fun deleteTransactionById(@Path("transactionId") transactionId: Long): Response<TransactionSuccessModel>

    @Deprecated("Use suspend function instead")
    @FormUrlEncoded
    @POST(TRANSACTION_API_ENDPOINT)
    fun addTransaction(@Field("transactions[0][type]") type: String,
                       @Field("transactions[0][description]") description: String,
                       @Field("transactions[0][date]") date: String,
                       @Field("transactions[0][piggy_bank_name]") piggyBankName: String?,
                       @Field("transactions[0][amount]") amount: String,
                       @Field("transactions[0][source_name]") sourceName: String?,
                       @Field("transactions[0][destination_name]") destinationName: String?,
                       @Field("transactions[0][currency_code]") currency: String,
                       @Field("transactions[0][category_name]") category: String?,
                       @Field("transactions[0][tags]") tags: String?,
                       @Field("transactions[0][budget_name]") budgetName: String?): Call<TransactionSuccessModel>

    @FormUrlEncoded
    @POST(TRANSACTION_API_ENDPOINT)
    suspend fun suspendAddTransaction(@Field("transactions[0][type]") type: String,
                       @Field("transactions[0][description]") description: String,
                       @Field("transactions[0][date]") date: String,
                       @Field("transactions[0][piggy_bank_name]") piggyBankName: String?,
                       @Field("transactions[0][amount]") amount: String,
                       @Field("transactions[0][source_name]") sourceName: String?,
                       @Field("transactions[0][destination_name]") destinationName: String?,
                       @Field("transactions[0][currency_code]") currency: String,
                       @Field("transactions[0][category_name]") category: String?,
                       @Field("transactions[0][tags]") tags: String?,
                       @Field("transactions[0][budget_name]") budgetName: String?): Response<TransactionSuccessModel>

    @FormUrlEncoded
    @PUT("$TRANSACTION_API_ENDPOINT/{transactionId}")
    fun updateTransaction(@Path("transactionId") transactionId: Long,
                          @Field("transactions[0][type]") type: String,
                          @Field("transactions[0][description]") description: String,
                          @Field("transactions[0][date]") date: String,
                          @Field("transactions[0][amount]") amount: String,
                          @Field("transactions[0][source_name]") sourceName: String?,
                          @Field("transactions[0][destination_name]") destinationName: String?,
                          @Field("transactions[0][currency_code]") currency: String,
                          @Field("transactions[0][category_name]") category: String?,
                          @Field("transactions[0][tags]") tags: String?,
                          @Field("transactions[0][budget_name]") budgetName: String?): Call<TransactionSuccessModel>

    @GET("$TRANSACTION_API_ENDPOINT/{id}/attachments")
    fun getTransactionAttachment(@Path("id") transactionId: Long): Call<AttachmentModel>

}