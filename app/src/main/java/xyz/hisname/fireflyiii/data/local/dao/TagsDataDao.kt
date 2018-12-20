package xyz.hisname.fireflyiii.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.tags.TagsData

@Dao
abstract class TagsDataDao: BaseDao<TagsData>{

    @Query("SELECT * FROM tags")
    abstract fun getAllTags(): LiveData<MutableList<TagsData>>

}