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

package xyz.hisname.fireflyiii.ui.categories

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.CategoryService
import xyz.hisname.fireflyiii.data.remote.firefly.api.SearchService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.category.CategoryPageSource
import xyz.hisname.fireflyiii.repository.category.CategorySearchPageSearch

class CategoriesDialogViewModel(application: Application): BaseViewModel(application) {

    private val categoryService = genericService().create(CategoryService::class.java)
    private val searchService = genericService().create(SearchService::class.java)
    private val categoryDao = AppDatabase.getInstance(application, getUniqueHash()).categoryDataDao()
    val categoryName = MutableLiveData<String>()

    // load everything on first load
    fun getCategoryList() = Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)){
        CategoryPageSource(categoryDao, categoryService)
    }.flow.cachedIn(viewModelScope).asLiveData()

    fun searchCategoryList(searchName: String) = Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)){
        CategorySearchPageSearch(searchName, categoryDao, searchService)
    }.flow.cachedIn(viewModelScope).asLiveData()

}
