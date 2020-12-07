package xyz.hisname.fireflyiii.repository.tags

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
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class TagsViewModel(application: Application): BaseViewModel(application) {

    val repository: TagsRepository
    private val tagsService by lazy { genericService()?.create(TagsService::class.java) }


    init {
        val tagsDataDao = AppDatabase.getInstance(application).tagsDataDao()
        repository = TagsRepository(tagsDataDao, tagsService)
    }

    fun getAllTags(): LiveData<MutableList<TagsData>> {
        isLoading.value = true
        var tagsData: MutableList<TagsData> = arrayListOf()
        val data: MutableLiveData<MutableList<TagsData>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            tagsData = repository.allTags()
        }.invokeOnCompletion {
            isLoading.postValue(false)
            data.postValue(tagsData)
        }
        return data
    }

    fun deleteTagByName(tagName: String): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        isLoading.value = true
        tagsService?.deleteTagByName(tagName)?.enqueue(retrofitCallback({ response ->
            if (response.code() == 204 || response.code() == 200) {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.deleteTagByName(tagName)
                }.invokeOnCompletion {
                    isDeleted.postValue(true)
                }
            } else {
                isDeleted.postValue(false)
            }
        })
        { throwable ->
            isDeleted.postValue(false)
        })
        return isDeleted
    }

    fun getTagById(tagId: Long): LiveData<TagsData>{
        val tagData = MutableLiveData<TagsData>()
        viewModelScope.launch(Dispatchers.IO){
            tagData.postValue(repository.getTagById(tagId))
        }
        return tagData
    }

    fun getTagByName(nameOfTag: String): LiveData<TagsData>{
        val tagData: MutableLiveData<TagsData> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            tagData.postValue(repository.getTagByName(nameOfTag))
        }
        return tagData
    }
}