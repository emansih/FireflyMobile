package xyz.hisname.fireflyiii.ui.tags

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.TagsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.tags.TagsData
import xyz.hisname.fireflyiii.repository.tags.TagsRepository
import xyz.hisname.fireflyiii.util.network.HttpConstants

class ListTagsViewModel(application: Application): BaseViewModel(application) {

    private val tagsRepository = TagsRepository(
            AppDatabase.getInstance(application).tagsDataDao(),
            genericService()?.create(TagsService::class.java)
    )

    fun getAllTags(): LiveData<MutableList<TagsData>> {
        isLoading.postValue(true)
        val data: MutableLiveData<MutableList<TagsData>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            data.postValue(tagsRepository.allTags())
            isLoading.postValue(false)
        }
        return data
    }

    fun deleteTagByName(tagName: String): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            when (tagsRepository.deleteTags(tagName)) {
                HttpConstants.FAILED -> {
                    isDeleted.postValue(false)
                }
                HttpConstants.UNAUTHORISED -> {
                    isDeleted.postValue(false)
                }
                HttpConstants.NO_CONTENT_SUCCESS -> {
                    isDeleted.postValue(true)
                }
            }
        }
        return isDeleted
    }
}