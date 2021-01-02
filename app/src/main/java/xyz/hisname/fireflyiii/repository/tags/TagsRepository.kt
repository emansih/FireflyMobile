package xyz.hisname.fireflyiii.repository.tags

import androidx.annotation.WorkerThread
import com.squareup.moshi.Moshi
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.TagsDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.TagsService
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.tags.TagsAttributes
import xyz.hisname.fireflyiii.repository.models.tags.TagsData
import xyz.hisname.fireflyiii.repository.models.tags.TagsSuccessModel
import xyz.hisname.fireflyiii.util.network.HttpConstants

@Suppress("RedundantSuspendModifier")
@WorkerThread
class TagsRepository(private val tagsDataDao: TagsDataDao,
                     private val tagsService: TagsService) {

    suspend fun allTags(): MutableList<TagsData>{
        try {
            val tagsData: MutableList<TagsData> = arrayListOf()
            val networkCall = tagsService.getPaginatedTags(1)
            val responseBody = networkCall.body()
            if (responseBody != null && networkCall.isSuccessful) {
                tagsData.addAll(responseBody.data.toMutableList())
                val pagination = responseBody.meta.pagination
                if (pagination.total_pages != pagination.current_page) {
                    for (items in 2..pagination.total_pages) {
                        tagsData.addAll(
                                tagsService.getPaginatedTags(items).body()?.data?.toMutableList()
                                        ?: arrayListOf())

                    }
                }
                tagsDataDao.deleteTags()
                tagsData.forEach { data ->
                    tagsDataDao.insert(data)
                }
            }
        } catch (exception: Exception){ }
        return tagsDataDao.getAllTags()
    }

    suspend fun getTagById(tagId: Long): TagsData {
        try {
            val tag = tagsDataDao.getTagById(tagId)
            if (tag.tagsAttributes.description.isEmpty()){
                val networkCall = tagsService.getTagByName(tagId.toString())
                val responseBody = networkCall.body()
                if(responseBody != null && networkCall.isSuccessful){
                    responseBody.data.forEach {  tagsData ->
                        tagsDataDao.insert(tagsData)
                    }
                }
            }
        } catch (exception: Exception){ }
        return tagsDataDao.getTagById(tagId)
    }

    suspend fun getTagByName(tagName: String): TagsData{
        try {
            val tag = tagsDataDao.getTagByName(tagName)
            if (tag.tagsAttributes.description.isEmpty()){
                val networkCall = tagsService.searchTag(tagName)
                val responseBody = networkCall.body()
                if(responseBody != null && networkCall.isSuccessful){
                    responseBody.forEach {  tagsItems ->
                        tagsDataDao.insert(TagsData(
                                TagsAttributes("","","",
                                        tagsItems.tag, "", "", "", ""),
                                tagsItems.id
                        ))
                    }
                }
            }
        } catch (exception: Exception){ }
        return tagsDataDao.getTagByName(tagName)
    }

    suspend fun searchTag(tagName: String): List<String>{
        try {
            val networkCall = tagsService.searchTag(tagName)
            val responseBody = networkCall.body()
            if(responseBody != null && networkCall.isSuccessful){
                responseBody.forEach {  tagsItems ->
                    tagsDataDao.insert(TagsData(
                            TagsAttributes("","","",
                                    tagsItems.tag, "", "", "", ""),
                            tagsItems.id
                    ))
                }
            }
        } catch (exception: Exception){
            exception.printStackTrace()
        }
        return tagsDataDao.searchTagByName("%$tagName%")
    }

    suspend fun addTags(tagName: String, date: String?, description: String?, latitude: String?,
                        longitude: String?, zoomLevel: String?): ApiResponses<TagsSuccessModel> {
        return try {
            val networkCall = tagsService.addTag(tagName, date, description, latitude, longitude, zoomLevel)
            parseResponse(networkCall)
        } catch (exception: Exception){
            ApiResponses(error = exception)
        }
    }

    suspend fun updateTags(tagId: Long, tagName: String, date: String?, description: String?, latitude: String?,
                        longitude: String?, zoomLevel: String?): ApiResponses<TagsSuccessModel> {
        return try {
            val networkCall = tagsService.updateTag(tagId, tagName, date, description, latitude, longitude, zoomLevel)
            parseResponse(networkCall)
        } catch (exception: Exception){
            ApiResponses(error = exception)
        }
    }

    private suspend fun parseResponse(responseFromServer: Response<TagsSuccessModel>): ApiResponses<TagsSuccessModel>{
        val responseBody = responseFromServer.body()
        val responseErrorBody = responseFromServer.errorBody()
        if(responseBody != null && responseFromServer.isSuccessful){
            tagsDataDao.insert(responseBody.data)
            return ApiResponses(response = responseBody)
        } else {
            if(responseErrorBody != null){
                // Ignore lint warning. False positive
                // https://github.com/square/retrofit/issues/3255#issuecomment-557734546
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(responseErrorBody.source())
                val errorMessage = when {
                    moshi?.errors?.longitude != null -> moshi.errors.longitude[0]
                    moshi?.errors?.tag != null -> moshi.errors.tag[0]
                    moshi?.errors?.latitude != null -> moshi.errors.latitude[0]
                    moshi?.errors?.zoomLevel != null -> moshi.errors.zoomLevel[0]
                    else -> moshi?.message ?: "Error occurred while saving tag"
                }
                return ApiResponses(errorMessage = errorMessage)
            }
            return ApiResponses(errorMessage = "Error occurred while saving tag")
        }
    }

    // Takes in tag id or tag name as parameter
    suspend fun deleteTags(tagName: String): Int{
        try {
            val networkResponse = tagsService.deleteTagByName(tagName)
            when (networkResponse.code()) {
                204 -> {
                    tagsDataDao.deleteTagByName(tagName)
                    return HttpConstants.NO_CONTENT_SUCCESS
                }
                401 -> {
                    /*   User is unauthenticated. We will retain user's data as we are
                     *   now in inconsistent state. This use case is unlikely to happen unless user
                     *   deletes their token from the web interface without updating the mobile client
                     */
                    return HttpConstants.UNAUTHORISED
                }
                404 -> {
                    // User probably deleted this on the web interface and tried to do it using mobile client
                    tagsDataDao.deleteTagByName(tagName)
                    return HttpConstants.NOT_FOUND
                }
                else -> {
                    return HttpConstants.FAILED
                }
            }
        } catch (exception: Exception){
            tagsDataDao.deleteTagByName(tagName)
            return HttpConstants.FAILED
        }
    }
}