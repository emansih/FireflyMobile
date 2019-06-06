package xyz.hisname.fireflyiii.data.remote.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.PIGGY_BANK_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyModel
import xyz.hisname.fireflyiii.repository.models.piggy.PiggySuccessModel

interface PiggybankService {

    @GET(PIGGY_BANK_API_ENDPOINT)
    suspend fun getPaginatedPiggyBank(@Query("page") type: Int): Response<PiggyModel>

    @DELETE("$PIGGY_BANK_API_ENDPOINT/{id}")
    fun deletePiggyBankById(@Path("id") id: Long): Call<PiggyModel>

    @GET("$PIGGY_BANK_API_ENDPOINT/{id}")
    fun getPiggyBankById(@Path("id") id: Long): Response<PiggyModel>

    @FormUrlEncoded
    @POST(PIGGY_BANK_API_ENDPOINT)
    fun createNewPiggyBank(@Field("name") name: String, @Field("account_id") accountId: String,
                           @Field("target_amount") targetAmount: String,
                           @Field("current_amount") currentAmount: String?,
                           @Field("start_date") startDate: String?, @Field("target_date") targetDate: String?,
                           @Field("notes") notes: String?): Call<PiggySuccessModel>
    @FormUrlEncoded
    @PUT("$PIGGY_BANK_API_ENDPOINT/{piggyId}")
    fun updatePiggyBank(@Path("piggyId") piggyId: Long,
                        @Field("name") name: String, @Field("account_id") accountId: String,
                        @Field("target_amount") targetAmount: String,
                        @Field("current_amount") currentAmount: String?,
                        @Field("start_date") startDate: String?, @Field("target_date") targetDate: String?,
                        @Field("notes") notes: String?): Call<PiggySuccessModel>
}