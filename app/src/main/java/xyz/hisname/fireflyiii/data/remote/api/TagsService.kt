package xyz.hisname.fireflyiii.data.remote.api

import retrofit2.Call
import retrofit2.http.GET
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.repository.models.tags.TagsModel

interface TagsService {

    @GET(Constants.TAGS_API_ENDPOINT)
    fun getAllTags(): Call<TagsModel>
}