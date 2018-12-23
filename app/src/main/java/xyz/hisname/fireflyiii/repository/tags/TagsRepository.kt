package xyz.hisname.fireflyiii.repository.tags

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.TagsDataDao
import xyz.hisname.fireflyiii.repository.models.tags.TagsData

class TagsRepository(private val tagsDataDao: TagsDataDao) {

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
    suspend fun allTags() = tagsDataDao.getAllTags()

}