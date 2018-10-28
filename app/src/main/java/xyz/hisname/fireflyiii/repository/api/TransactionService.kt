package xyz.hisname.fireflyiii.repository.api

import retrofit2.Call
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.TRANSACTION_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionModel
import xyz.hisname.fireflyiii.repository.models.transaction.sucess.TransactionSucessModel

// Link to relevant doc: https://firefly-iii.readthedocs.io/en/latest/api/transactions.html
interface TransactionService {

    /* Start date and end date is an optional query. Do not pass NULL into it, instead pass
    an empty string to it.
    */
    @GET(TRANSACTION_API_ENDPOINT)
    fun getAllTransactions(@Query("start") startDate: String?,
                           @Query("end") endDate: String?,
                           @Query("type") type: String): Call<TransactionModel>

    @GET("$TRANSACTION_API_ENDPOINT/{id}")
    fun getTransactionById(@Path("id") id: String)

    @DELETE("$TRANSACTION_API_ENDPOINT/{id}")
    fun deleteTransactionById(@Path("id") id: String)

    @FormUrlEncoded
    @POST(TRANSACTION_API_ENDPOINT)
    fun addTransaction(@Field("type") type: String, @Field("description") description: String,
                       @Field("date") date: String, @Field("piggy_bank_name") piggyBankName: String?,
                       @Field("bill_name") billName: String?, @Field("transactions[0][amount]") amount: String,
                       @Field("transactions[0][source_name]") sourceName: String?,
                       @Field("transactions[0][destination_name]") destionationName: String?,
                       @Field("transactions[0][currency_code]") currency: String,
                       @Field("transactions[0][category_name]") category: String?): Call<TransactionSucessModel>
}