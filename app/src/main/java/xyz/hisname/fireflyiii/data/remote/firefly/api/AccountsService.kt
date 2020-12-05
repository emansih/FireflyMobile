package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.Constants.Companion.ACCOUNTS_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.accounts.AccountsModel
import xyz.hisname.fireflyiii.repository.models.accounts.AccountSuccessModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionModel

// Link to relevant doc: https://firefly-iii.readthedocs.io/en/latest/api/accounts.html
interface AccountsService {

    @GET(ACCOUNTS_API_ENDPOINT)
    suspend fun getPaginatedAccountType(@Query("type") type: String,
                                        @Query("page") page: Int): Response<AccountsModel>

    @FormUrlEncoded
    @POST(ACCOUNTS_API_ENDPOINT)
    fun addAccount(@Field("name") name: String,
                   @Field("type") type: String,
                   @Field("currency_code") currencyCode: String?,
                   @Field("iban") iban: String?,
                   @Field("bic") bic: String?,
                   @Field("account_number") accountNumber: String?,
                   @Field("opening_balance") openingBalance: String?,
                   @Field("opening_balance_date") openingBalanceDate: String?,
                   @Field("account_role") accountRole: String?,
                   @Field("virtual_balance") virtualBalance: String?,
                   @Field("include_net_worth") includeNetWorth: Boolean,
                   @Field("notes") notes: String?,
                   @Field("liability_type") liabilityType: String?,
                   @Field("liability_amount") liabilityAmount: String?,
                   @Field("liability_start_date") liabilityStartDate: String?,
                   @Field("interest") interest: String?,
                   @Field("interest_period") interestPeriod: String?): Call<AccountSuccessModel>

    @FormUrlEncoded
    @PUT("$ACCOUNTS_API_ENDPOINT/{accountId}")
    fun updateAccount(@Path("accountId") accountId: Long,
                      @Field("name") name: String,
                      @Field("type") type: String,
                      @Field("currency_code") currencyCode: String?,
                      @Field("iban") iban: String?,
                      @Field("bic") bic: String?,
                      @Field("account_number") accountNumber: String?,
                      @Field("opening_balance") openingBalance: String?,
                      @Field("opening_balance_date") openingBalanceDate: String?,
                      @Field("account_role") accountRole: String?,
                      @Field("virtual_balance") virtualBalance: String?,
                      @Field("include_net_worth") includeNetWorth: Boolean,
                      @Field("notes") notes: String?,
                      @Field("liability_type") liabilityType: String?,
                      @Field("liability_amount") liabilityAmount: String?,
                      @Field("liability_start_date") liabilityStartDate: String?,
                      @Field("interest") interest: String?,
                      @Field("interest_period") interestPeriod: String?): Call<AccountSuccessModel>

    @DELETE("$ACCOUNTS_API_ENDPOINT/{id}")
    suspend fun deleteAccountById(@Path("id") id: Long): Response<AccountsModel>

    @GET("${Constants.SEARCH_API_ENDPOINT}/accounts")
    suspend fun searchAccount(@Query("query") query: String,
                              @Query("type") type: String,
                              @Query("field") field: String = "name"): Response<AccountsModel>

    @GET("${Constants.SEARCH_API_ENDPOINT}/accounts/{id}/transactions")
    suspend fun getTransactionsByAccountId(@Path("id") id: Long,
                                           @Query("page") page: Int,
                                           @Query("start") startDate: String,
                                           @Query("end") endDate: String,
                                           @Query("type") type: String): Response<TransactionModel>
}