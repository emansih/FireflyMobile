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

package xyz.hisname.fireflyiii.repository.category

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.data.local.dao.CategoryDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.CategoryService
import xyz.hisname.fireflyiii.repository.models.category.CategoryAttributes
import xyz.hisname.fireflyiii.repository.models.category.CategoryData

class CategorySearchPageSearch(private val searchName: String,
                               private val categoryDataDao: CategoryDataDao,
                               private val categoryService: CategoryService): PagingSource<Int, CategoryData>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CategoryData> {
        return try {
            val networkCall = categoryService.searchCategory(searchName)
            val responseBody = networkCall.body()
            if (responseBody != null && networkCall.isSuccessful) {
                responseBody.forEach { category ->
                    categoryDataDao.insert(CategoryData(category.id, CategoryAttributes("", "", category.name, "")))
                }
            }
            LoadResult.Page(categoryDataDao.searchCategory("*$searchName*"), null, null)
        } catch (exception: Exception){
            LoadResult.Page(categoryDataDao.searchCategory("*$searchName*"), null, null)
        }
    }

    override val keyReuseSupported = true
}