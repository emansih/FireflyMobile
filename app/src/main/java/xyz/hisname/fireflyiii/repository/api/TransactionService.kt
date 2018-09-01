package xyz.hisname.fireflyiii.repository.api

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import xyz.hisname.fireflyiii.Constants.Companion.TRANSACTION_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionModel

// Link to relevant doc: https://firefly-iii.readthedocs.io/en/latest/api/transactions.html
interface TransactionService {

    /* Start date and end date is an optional query. Do not pass NULL into it, instead pass
    an empty string to it.
    */
    @GET(TRANSACTION_API_ENDPOINT)
    fun getAllTransactions(@Query("start") startDate: String,
                           @Query("end") endDate: String,
                           @Query("type") type: String): Call<TransactionModel>

    @GET("$TRANSACTION_API_ENDPOINT/{id}")
    fun getTransactionById(@Path("id") id: String)

    @DELETE("$TRANSACTION_API_ENDPOINT/{id}")
    fun deleteTransactionById(@Path("id") id: String)

}