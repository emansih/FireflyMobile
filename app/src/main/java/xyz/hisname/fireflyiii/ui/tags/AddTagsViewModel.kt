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

package xyz.hisname.fireflyiii.ui.tags

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.TagsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.tags.TagsData
import xyz.hisname.fireflyiii.repository.tags.TagsRepository

class AddTagsViewModel(application: Application): BaseViewModel(application) {

    private val tagsRepository = TagsRepository(
            AppDatabase.getInstance(application, getUniqueHash()).tagsDataDao(),
            genericService().create(TagsService::class.java)
    )

    fun addTag(tagName: String, date: String?, description: String?, latitude: String?,
               longitude: String?, zoomLevel: String?): LiveData<Pair<Boolean, String>> {
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            val addTags = tagsRepository.addTags(tagName, date, description, latitude, longitude, zoomLevel)
            when {
                addTags.response != null -> {
                    apiResponse.postValue(Pair(true,
                            getApplication<Application>().getString(R.string.tag_created, tagName)))
                }
                addTags.errorMessage != null -> {
                    apiResponse.postValue(Pair(false,addTags.errorMessage))
                }
                addTags.error != null -> {
                    apiResponse.postValue(Pair(false,addTags.error.localizedMessage))
                }
                else -> {
                    apiResponse.postValue(Pair(false, "Error saving tags"))
                }
            }
            isLoading.postValue(false)
        }
        return apiResponse
    }

    fun updateTag(tagId: Long, tagName: String, date: String?, description: String?, latitude: String?,
                  longitude: String?, zoomLevel: String?): LiveData<Pair<Boolean, String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            val addTags = tagsRepository.updateTags(tagId, tagName, date, description, latitude, longitude, zoomLevel)
            when {
                addTags.response != null -> {
                    apiResponse.postValue(Pair(true,
                            getApplication<Application>().getString(R.string.tag_updated, tagName)))
                }
                addTags.errorMessage != null -> {
                    apiResponse.postValue(Pair(false,addTags.errorMessage))
                }
                addTags.error != null -> {
                    apiResponse.postValue(Pair(false,addTags.error.localizedMessage))
                }
                else -> {
                    apiResponse.postValue(Pair(false, "Error updating tags"))
                }
            }
            isLoading.postValue(false)
        }
        return apiResponse
    }

    fun getTagById(tagId: Long): LiveData<TagsData>{
        val tagLiveData = MutableLiveData<TagsData>()
        viewModelScope.launch(Dispatchers.IO){
            tagLiveData.postValue(tagsRepository.getTagById(tagId))
        }
        return tagLiveData
    }
}