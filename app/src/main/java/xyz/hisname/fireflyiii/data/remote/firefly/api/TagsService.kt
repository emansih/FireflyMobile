package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.AUTOCOMPLETE_API_ENDPOINT
import xyz.hisname.fireflyiii.Constants.Companion.TAGS_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.autocomplete.TagsItems
import xyz.hisname.fireflyiii.repository.models.tags.TagsModel
import xyz.hisname.fireflyiii.repository.models.tags.TagsSuccessModel

interface TagsService {

    @GET(TAGS_API_ENDPOINT)
    suspend fun getPaginatedTags(@Query("page") page: Int): Response<TagsModel>

    @DELETE("$TAGS_API_ENDPOINT/{tagName}")
    suspend fun deleteTagByName(@Path("tagName") tagName: String): Response<TagsModel>

    @FormUrlEncoded
    @POST(TAGS_API_ENDPOINT)
    suspend fun addTag(@Field("tag") tagName: String,
                       @Field("date") date: String?,
                       @Field("description") description: String?,
                       @Field("latitude") latitude: String?,
                       @Field("longitude") longitude: String?,
                       @Field("zoom_level") zoomLevel: String?): Response<TagsSuccessModel>

    @FormUrlEncoded
    @PUT("$TAGS_API_ENDPOINT/{tagId}")
    suspend fun updateTag(@Path("tagId") tagId: Long,
                          @Field("tag") tagName: String,
                          @Field("date") date: String?,
                          @Field("description") description: String?,
                          @Field("latitude") latitude: String?,
                          @Field("longitude") longitude: String?,
                          @Field("zoom_level") zoomLevel: String?): Response<TagsSuccessModel>

    // Takes in either tag name(string) or tag id(long) as a parameter
    @GET("$TAGS_API_ENDPOINT/{tagName}")
    suspend fun getTagByName(@Path("tagName") tagName: String): Response<TagsModel>

    @GET("$AUTOCOMPLETE_API_ENDPOINT/{tags}")
    suspend fun searchTag(@Query("query") queryString: String): Response<List<TagsItems>>
}