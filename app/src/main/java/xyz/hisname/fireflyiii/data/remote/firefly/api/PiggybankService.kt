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
import xyz.hisname.fireflyiii.Constants.Companion.PIGGY_BANK_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentModel
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
                             @Field("notes") notes: String?,
                             @Field("group") group: String?): Response<PiggySuccessModel>
    @FormUrlEncoded
    @PUT("$PIGGY_BANK_API_ENDPOINT/{piggyId}")
    suspend fun updatePiggyBank(@Path("piggyId") piggyId: Long,
                                @Field("name") name: String,
                                @Field("account_id") accountId: Long,
                                @Field("target_amount") targetAmount: String,
                                @Field("current_amount") currentAmount: String?,
                                @Field("start_date") startDate: String?,
                                @Field("target_date") targetDate: String?,
                                @Field("notes") notes: String?,
                                @Field("group") group: String?): Response<PiggySuccessModel>

    @GET("${PIGGY_BANK_API_ENDPOINT}/{id}/attachments")
    suspend fun getPiggyBankAttachment(@Path("id") piggyId: Long): Response<AttachmentModel>

}