package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.BILL_API_ENDPONT
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.bills.BillsModel
import xyz.hisname.fireflyiii.repository.models.bills.BillSuccessModel

// Link to relevant doc: https://firefly-iii.readthedocs.io/en/latest/api/bills.html
interface BillsService {

    @GET(BILL_API_ENDPONT)
    suspend fun getPaginatedBills(@Query("page") type: Int): Response<BillsModel>

    @GET("$BILL_API_ENDPONT/{id}")
    suspend fun getBillById(@Path("id") id: Long): Response<BillsModel>

    @FormUrlEncoded
    @POST(BILL_API_ENDPONT)
    fun createBill(@Field("name") name: String, @Field("amount_min") amountMin: String,
                   @Field("amount_max") amountMax: String, @Field("date") date: String,
                   @Field("repeat_freq") repeatFreq: String, @Field("skip") skip: String,
                   @Field("active") active: String, @Field("currency_code") currencyCode: String,
                   @Field("notes") notes: String?
    ): Call<BillSuccessModel>

    @DELETE("$BILL_API_ENDPONT/{id}")
    suspend fun deleteBillById(@Path("id") id: Long): Response<BillsModel>

    @FormUrlEncoded
    @PUT("$BILL_API_ENDPONT/{id}")
    fun updateBill(@Path("id") id: Long, @Field("name") name: String,
                   @Field("amount_min") amountMin: String, @Field("amount_max") amountMax: String,
                   @Field("date") date: String, @Field("repeat_freq") repeatFreq: String,
                   @Field("skip") skip: String, @Field("active") active: String,
                   @Field("currency_code") currencyCode: String, @Field("notes") notes: String?
    ): Call<BillSuccessModel>


}