package xyz.hisname.fireflyiii.repository.tags

import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.TagsDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.TagsService
import xyz.hisname.fireflyiii.repository.models.tags.TagsData
import xyz.hisname.fireflyiii.repository.models.tags.TagsModel

class TagsRepository(private val tagsDataDao: TagsDataDao,
                     private val tagsService: TagsService?) {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertTags(tags: TagsData){
        tagsDataDao.insert(tags)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteTagByName(tagName: String): Int{
        return tagsDataDao.deleteTagByName(tagName)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun allTags(): MutableList<TagsData>{
        var networkCall: Response<TagsModel>? = null
        val tagsData: MutableList<TagsData> = arrayListOf()
        try {
            withContext(Dispatchers.IO) {
                withContext(Dispatchers.IO) {
                    networkCall = tagsService?.getPaginatedTags(1)
                }
                tagsData.addAll(networkCall?.body()?.data?.toMutableList() ?: arrayListOf())
            }
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall?.isSuccessful != false) {
                val pagination = responseBody.meta.pagination
                if (pagination.total_pages != pagination.current_page) {
                    withContext(Dispatchers.IO) {
                        for (items in 2..pagination.total_pages) {
                            tagsData.addAll(
                                    tagsService?.getPaginatedTags(items)?.body()?.data?.toMutableList()
                                            ?: arrayListOf()
                            )
                        }
                    }
                }
                withContext(Dispatchers.IO) {
                    tagsDataDao.deleteTags()
                }
                withContext(Dispatchers.IO) {
                    tagsData.forEachIndexed { _, data ->
                        insertTags(data)
                    }
                }
            }
        } catch (exception: Exception){ }

        return tagsDataDao.getAllTags()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun retrieveTagById(tagId: Long) = tagsDataDao.getTagById(tagId)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun retrieveTagByName(tagName: String) = tagsDataDao.getTagByName(tagName)

}