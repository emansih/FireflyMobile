package xyz.hisname.fireflyiii.data.remote.api

import retrofit2.Call
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.Constants.Companion.TAGS_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.tags.TagsModel
import xyz.hisname.fireflyiii.repository.models.tags.TagsSuccessModel

interface TagsService {

    @GET(TAGS_API_ENDPOINT)
    fun getAllTags(): Call<TagsModel>

    @DELETE("$TAGS_API_ENDPOINT/{tagName}")
    fun deleteTagByName(@Path("tagName") tagName: String): Call<TagsModel>

    @FormUrlEncoded
    @POST(TAGS_API_ENDPOINT)
    fun createNewTag(@Field("tag") tagName: String,@Field("date") date: String?,
                     @Field("description") description: String?,
                     @Field("latitude") latitude: String?, @Field("longitude") longitude: String?,
                     @Field("zoom_level") zoomLevel: String?): Call<TagsSuccessModel>

    @FormUrlEncoded
    @PUT("$TAGS_API_ENDPOINT/{tagId}")
    fun updateTag(@Path("tagId") tagId: Long,
                  @Field("tag") tagName: String, @Field("date") date: String?,
                  @Field("description") description: String?,
                  @Field("latitude") latitude: String?, @Field("longitude") longitude: String?,
                  @Field("zoom_level") zoomLevel: String?): Call<TagsSuccessModel>

    @GET("$TAGS_API_ENDPOINT/{tagName}")
    fun getTagByName(@Path("tagName") tagName: String): Call<TagsModel>

}