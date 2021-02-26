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
import xyz.hisname.fireflyiii.Constants.Companion.BILL_API_ENDPONT
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentModel
import xyz.hisname.fireflyiii.repository.models.bills.BillsModel
import xyz.hisname.fireflyiii.repository.models.bills.BillSuccessModel
import xyz.hisname.fireflyiii.repository.models.bills.SingleBillModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionModel

interface BillsService {

    @GET(BILL_API_ENDPONT)
    suspend fun getPaginatedBills(@Query("page") type: Int): Response<BillsModel>

    @GET(BILL_API_ENDPONT)
    suspend fun getPaginatedBills(@Query("page") type: Int,
                                  @Query("start") startDate: String,
                                  @Query("end") endDate: String): Response<BillsModel>

    @GET("$BILL_API_ENDPONT/{id}")
    suspend fun getBillById(@Path("id") id: Long): Response<BillsModel>

    @GET("$BILL_API_ENDPONT/{id}")
    suspend fun getBillById(@Path("id") id: Long,
                            @Query("start") startDate: String,
                            @Query("end") endDate: String): Response<SingleBillModel>

    @GET("$BILL_API_ENDPONT/{id}/transactions")
    suspend fun getTransactionFromBillById(@Path("id") id: Long,
                                           @Query("start") startDate: String): Response<TransactionModel>

    @FormUrlEncoded
    @POST(BILL_API_ENDPONT)
    suspend fun createBill(@Field("name") name: String,
                           @Field("amount_min") amountMin: String,
                           @Field("amount_max") amountMax: String,
                           @Field("date") date: String,
                           @Field("repeat_freq") repeatFreq: String,
                           @Field("skip") skip: String,
                           @Field("active") active: String,
                           @Field("currency_code") currencyCode: String,
                           @Field("notes") notes: String?
    ): Response<BillSuccessModel>

    @DELETE("$BILL_API_ENDPONT/{id}")
    suspend fun deleteBillById(@Path("id") id: Long): Response<BillsModel>

    @FormUrlEncoded
    @PUT("$BILL_API_ENDPONT/{id}")
    suspend fun updateBill(@Path("id") id: Long,
                           @Field("name") name: String,
                           @Field("amount_min") amountMin: String,
                           @Field("amount_max") amountMax: String,
                           @Field("date") date: String,
                           @Field("repeat_freq") repeatFreq: String,
                           @Field("skip") skip: String,
                           @Field("active") active: String,
                           @Field("currency_code") currencyCode: String,
                           @Field("notes") notes: String?): Response<BillSuccessModel>

    @GET("${BILL_API_ENDPONT}/{id}/attachments")
    suspend fun getBillAttachment(@Path("id") billId: Long): Response<AttachmentModel>

}