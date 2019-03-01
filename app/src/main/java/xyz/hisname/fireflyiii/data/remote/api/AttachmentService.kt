package xyz.hisname.fireflyiii.data.remote.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.ATTACHMENT_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentModel

interface AttachmentService {

    @Multipart
    @POST("$ATTACHMENT_API_ENDPOINT/{id}/upload")
    fun uploadFile(@Path("id") id: Long,
                   @Part("description") description: RequestBody,
                   @Part file: MultipartBody.Part): Call<Void>

    @FormUrlEncoded
    @POST(ATTACHMENT_API_ENDPOINT)
    fun storeAttachment(@Field("filename") filename: String,
                        @Field("model") model: String,
                        @Field("model_id") modelId: Long,
                        @Field("title") title: String,
                        @Field("notes") notes: String): Call<AttachmentModel>
}