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
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.repository.models.category.CategoryFts


@Dao
abstract class CategoryDataDao: BaseDao<CategoryData> {

    @Query("DELETE FROM category WHERE categoryId = :categoryId")
    abstract fun deleteCategoryById(categoryId: Long): Int

    @Query("DELETE FROM category")
    abstract suspend fun deleteAllCategory(): Int

    @Query("SELECT * FROM category order by categoryId desc limit :limitNumber")
    abstract fun getPaginatedCategory(limitNumber: Int): Flow<MutableList<CategoryData>>

    @Query("SELECT * FROM category order by categoryId")
    abstract suspend fun getCategory(): List<CategoryData>

    @Query("SELECT COUNT(*) FROM category")
    abstract suspend fun getCategoryCount(): Int

    @Query("SELECT * FROM category JOIN categoryFts ON category.categoryId == categoryFts.categoryId WHERE categoryFts MATCH :categoryName GROUP BY categoryFts.categoryId")
    abstract suspend fun searchCategory(categoryName: String): List<CategoryData>

    @Query("SELECT * FROM category WHERE categoryId =:categoryId")
    abstract fun getCategoryById(categoryId: Long): CategoryData
}