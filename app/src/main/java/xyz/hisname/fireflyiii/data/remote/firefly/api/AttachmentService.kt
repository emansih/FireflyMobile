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

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.ATTACHMENT_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.attachment.Attachment

interface AttachmentService {

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("$ATTACHMENT_API_ENDPOINT/{id}/upload")
    suspend fun uploadFile(@Path("id") id: Long,
                           @Body file: RequestBody): Response<Void>

    @FormUrlEncoded
    @POST(ATTACHMENT_API_ENDPOINT)
    suspend fun storeAttachment(@Field("filename") filename: String,
                        @Field("attachable_type") attachable_type: String,
                        @Field("attachable_id") attachable_id: Long,
                        @Field("title") title: String,
                        @Field("notes") notes: String): Response<Attachment>

    @DELETE("$ATTACHMENT_API_ENDPOINT/{id}")
    suspend fun deleteAttachment(@Path("id") id: Long): Response<Void>

}