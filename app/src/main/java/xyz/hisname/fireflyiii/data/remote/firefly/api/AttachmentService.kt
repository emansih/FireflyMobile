package xyz.hisname.fireflyiii.data.remote.firefly.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.ATTACHMENT_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.attachment.Attachment
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentModel

interface AttachmentService {

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("$ATTACHMENT_API_ENDPOINT/{id}/upload")
    fun uploadFile(@Path("id") id: Long,
                   @Body file: RequestBody): Call<Void>

    @FormUrlEncoded
    @POST(ATTACHMENT_API_ENDPOINT)
    fun storeAttachment(@Field("filename") filename: String,
                        @Field("model") model: String,
                        @Field("model_id") modelId: Long,
                        @Field("title") title: String,
                        @Field("notes") notes: String): Call<Attachment>

    //@Streaming
    @GET
    fun downloadFile(@Url fileUrl: String?): Call<ResponseBody>

}