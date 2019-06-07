package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.tags.TagsData

@Dao
abstract class TagsDataDao: BaseDao<TagsData>{

    @Query("SELECT * FROM tags")
    abstract fun getAllTags(): MutableList<TagsData>

    @Query("DELETE FROM tags WHERE tag = :tagName")
    abstract fun deleteTagByName(tagName: String): Int

    @Query("SELECT * FROM tags WHERE tagsId = :tagId")
    abstract fun getTagById(tagId: Long): MutableList<TagsData>

    @Query("SELECT * FROM tags WHERE tag = :nameOfTag")
    abstract fun getTagByName(nameOfTag: String): MutableList<TagsData>

    @Query("DELETE FROM tags")
    abstract fun deleteTags(): Int

}