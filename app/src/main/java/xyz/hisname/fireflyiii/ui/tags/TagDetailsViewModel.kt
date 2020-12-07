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

class TagDetailsViewModel(application: Application): BaseViewModel(application) {

    private val tagsRepository = TagsRepository(
            AppDatabase.getInstance(application).tagsDataDao(),
            genericService()?.create(TagsService::class.java)
    )

    fun getTagById(tagId: Long): LiveData<TagsData> {
        val tagData = MutableLiveData<TagsData>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            tagData.postValue(tagsRepository.getTagById(tagId))
            isLoading.postValue(false)
        }
        return tagData
    }

    fun getTagByName(nameOfTag: String): LiveData<TagsData>{
        val tagData: MutableLiveData<TagsData> = MutableLiveData()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            tagData.postValue(tagsRepository.getTagByName(nameOfTag))
            isLoading.postValue(false)
        }
        return tagData
    }

    fun deleteTagByName(tagName: String): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            when (tagsRepository.deleteTagByName(tagName)) {
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