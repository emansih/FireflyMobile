package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.Response
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.Constants.Companion.PIGGY_BANK_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentModel
import xyz.hisname.fireflyiii.repository.models.autocomplete.PiggybankItems
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyModel
import xyz.hisname.fireflyiii.repository.models.piggy.PiggySuccessModel

interface PiggybankService {

    @GET(PIGGY_BANK_API_ENDPOINT)
    suspend fun getPaginatedPiggyBank(@Query("page") type: Int): Response<PiggyModel>

    @DELETE("$PIGGY_BANK_API_ENDPOINT/{id}")
    suspend fun deletePiggyBankById(@Path("id") piggyId: Long): Response<PiggyModel>

    @FormUrlEncoded
    @POST(PIGGY_BANK_API_ENDPOINT)
    suspend fun addPiggyBank(@Field("name") name: String,
                     @Field("account_id") accountId: Long,
                     @Field("target_amount") targetAmount: String,
                     @Field("current_amount") currentAmount: String?,
                     @Field("start_date") startDate: String?,
                     @Field("target_date") targetDate: String?,
                     @Field("notes") notes: String?): Response<PiggySuccessModel>
    @FormUrlEncoded
    @PUT("$PIGGY_BANK_API_ENDPOINT/{piggyId}")
    suspend fun updatePiggyBank(@Path("piggyId") piggyId: Long,
                        @Field("name") name: String,
                        @Field("account_id") accountId: Long,
                        @Field("target_amount") targetAmount: String,
                        @Field("current_amount") currentAmount: String?,
                        @Field("start_date") startDate: String?,
                        @Field("target_date") targetDate: String?,
                        @Field("notes") notes: String?): Response<PiggySuccessModel>

    @GET("${Constants.SEARCH_API_ENDPOINT}/piggy-banks")
    suspend fun searchPiggybank(query: String): Response<List<PiggybankItems>>

    @GET("${PIGGY_BANK_API_ENDPOINT}/{id}/attachments")
    suspend fun getPiggyBankAttachment(@Path("id") piggyId: Long): Response<AttachmentModel>

}