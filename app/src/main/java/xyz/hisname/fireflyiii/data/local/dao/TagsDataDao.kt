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
    abstract fun getTagById(tagId: Long): TagsData

    @Query("SELECT * FROM tags WHERE tag = :nameOfTag")
    abstract fun getTagByName(nameOfTag: String): TagsData

    @Query("SELECT description FROM tags WHERE description LIKE :name")
    abstract fun searchTagByName(name: String): List<String>

    @Query("SELECT * FROM tags WHERE description LIKE :name")
    abstract fun searchTags(name: String): List<TagsData>

    @Query("DELETE FROM tags")
    abstract fun deleteTags(): Int

}