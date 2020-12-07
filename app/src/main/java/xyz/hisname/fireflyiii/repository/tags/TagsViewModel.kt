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
}