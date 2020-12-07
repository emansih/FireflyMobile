package xyz.hisname.fireflyiii.repository.tags

import androidx.annotation.WorkerThread
import com.squareup.moshi.Moshi
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.TagsDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.TagsService
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.tags.TagsData
import xyz.hisname.fireflyiii.repository.models.tags.TagsSuccessModel

@Suppress("RedundantSuspendModifier")
@WorkerThread
class TagsRepository(private val tagsDataDao: TagsDataDao,
                     private val tagsService: TagsService?) {

    suspend fun insertTags(tags: TagsData){
        tagsDataDao.insert(tags)
    }

    suspend fun deleteTagByName(tagName: String): Int{
        return tagsDataDao.deleteTagByName(tagName)
    }

    suspend fun allTags(): MutableList<TagsData>{
        try {
            val tagsData: MutableList<TagsData> = arrayListOf()
            val networkCall = tagsService?.getPaginatedTags(1)
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                tagsData.addAll(responseBody.data.toMutableList())
                val pagination = responseBody.meta.pagination
                if (pagination.total_pages != pagination.current_page) {
                    for (items in 2..pagination.total_pages) {
                        tagsData.addAll(
                                tagsService?.getPaginatedTags(items)?.body()?.data?.toMutableList()
                                        ?: arrayListOf())

                    }
                }
                tagsDataDao.deleteTags()
                tagsData.forEach { data ->
                    insertTags(data)
                }
            }
        } catch (exception: Exception){ }
        return tagsDataDao.getAllTags()
    }

    suspend fun getTagById(tagId: Long): TagsData {
        try {
            val tag = tagsDataDao.getTagById(tagId)
            if (tag.tagsAttributes.description.isEmpty()){
                val networkCall = tagsService?.getTagByName(tagId.toString())
                val responseBody = networkCall?.body()
                if(responseBody != null && networkCall.isSuccessful){
                    responseBody.data.forEach {  tagsData ->
                        insertTags(tagsData)
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
                val networkCall = tagsService?.searchTag(tagName)
                val responseBody = networkCall?.body()
                if(responseBody != null && networkCall.isSuccessful){
                    getTagById(responseBody[0].id)
                }
            }
        } catch (exception: Exception){ }
        return tagsDataDao.getTagByName(tagName)
    }

    suspend fun addTags(tagName: String, date: String?, description: String?, latitude: String?,
                        longitude: String?, zoomLevel: String?): ApiResponses<TagsSuccessModel> {
        return try {
            val networkCall = tagsService?.addTag(tagName, date, description, latitude, longitude, zoomLevel)
            parseResponse(networkCall)
        } catch (exception: Exception){
            ApiResponses(error = exception)
        }
    }

    suspend fun updateTags(tagId: Long, tagName: String, date: String?, description: String?, latitude: String?,
                        longitude: String?, zoomLevel: String?): ApiResponses<TagsSuccessModel> {
        return try {
            val networkCall = tagsService?.updateTag(tagId, tagName, date, description, latitude, longitude, zoomLevel)
            parseResponse(networkCall)
        } catch (exception: Exception){
            ApiResponses(error = exception)
        }
    }

    private suspend fun parseResponse(responseFromServer: Response<TagsSuccessModel>?): ApiResponses<TagsSuccessModel>{
        val responseBody = responseFromServer?.body()
        val responseErrorBody = responseFromServer?.errorBody()
        if(responseBody != null && responseFromServer.isSuccessful){
            if(responseErrorBody != null){
                // Ignore lint warning. False positive
                // https://github.com/square/retrofit/issues/3255#issuecomment-557734546
                var errorMessage = String(responseErrorBody.bytes())
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorMessage)
                errorMessage = when {
                    moshi?.errors?.longitude != null -> moshi.errors.longitude[0]
                    moshi?.errors?.tag != null -> moshi.errors.tag[0]
                    moshi?.errors?.latitude != null -> moshi.errors.latitude[0]
                    moshi?.errors?.zoomLevel != null -> moshi.errors.zoomLevel[0]
                    else -> "Error occurred while saving tag"
                }
                return ApiResponses(errorMessage = errorMessage)
            } else {
                insertTags(responseBody.data)
                return ApiResponses(response = responseBody)
            }
        } else {
            return ApiResponses(errorMessage = "Error occurred while saving tag")
        }
    }

}