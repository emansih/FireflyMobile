package xyz.hisname.fireflyiii.data.remote.api

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.repository.models.tags.TagsModel

interface TagsService {

    @GET(Constants.TAGS_API_ENDPOINT)
    fun getAllTags(): Call<TagsModel>

    @DELETE("${Constants.TAGS_API_ENDPOINT}/{tagName}")
    fun deleteTagByName(@Path("tagName") tagName: String): Call<TagsModel>
}