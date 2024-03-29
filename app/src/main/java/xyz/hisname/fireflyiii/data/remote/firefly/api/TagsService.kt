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
import xyz.hisname.fireflyiii.Constants.Companion.TAGS_API_ENDPOINT
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
}