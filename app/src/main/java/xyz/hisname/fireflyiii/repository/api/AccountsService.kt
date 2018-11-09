package xyz.hisname.fireflyiii.repository.api

import retrofit2.Call
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.ACCOUNTS_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.accounts.AccountsModel
import xyz.hisname.fireflyiii.repository.models.accounts.AccountSuccessModel

// Link to relevant doc: https://firefly-iii.readthedocs.io/en/latest/api/accounts.html
interface AccountsService {

    @GET(ACCOUNTS_API_ENDPOINT)
    fun getAccountType(@Query("type") type: String): Call<AccountsModel>

    @FormUrlEncoded
    @POST(ACCOUNTS_API_ENDPOINT)
    fun addAccount(@Field("name") name: String,
                   @Field("type") type: String,
                   @Field("currency_code") currencyCode: String,
                   @Field("active") active: Int,
                   @Field("include_net_worth") includeNetWorth: Int,
                   @Field("account_role") accountRole: String?,
                   @Field("cc_type") ccType: String?,
                   @Field("cc_monthly_payment_date") ccMonthlyPaymentDate: String?,
                   @Field("liability_type") liabilityType: String?,
                   @Field("liability_amount") liabilityAmount: String?,
                   @Field("liability_start_date") liabilityStartDate: String?,
                   @Field("interest") interest: String?,
                   @Field("interest_period") interestPeriod: String?,
                   @Field("account_number") accountNumber: String?): Call<AccountSuccessModel>

    @DELETE("$ACCOUNTS_API_ENDPOINT/{id}")
    fun deleteAccountById(@Path("id") id: String): Call<AccountsModel>

}