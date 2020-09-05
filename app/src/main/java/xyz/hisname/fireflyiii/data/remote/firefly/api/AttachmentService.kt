package xyz.hisname.fireflyiii.data.remote.firefly.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
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

    @Streaming
    @GET
    fun downloadFile(@Url fileUrl: String?): Call<ResponseBody>

}