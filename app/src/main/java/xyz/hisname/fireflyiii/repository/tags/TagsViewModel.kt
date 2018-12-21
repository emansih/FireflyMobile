package xyz.hisname.fireflyiii.repository.tags

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.api.TagsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.tags.TagsData
import xyz.hisname.fireflyiii.util.network.NetworkErrors
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class TagsViewModel(application: Application): BaseViewModel(application) {

    val repository: TagsRepository
    private val tagsService by lazy { genericService()?.create(TagsService::class.java) }


    init {
        val tagsDataDao = AppDatabase.getInstance(application).tagsDataDao()
        repository = TagsRepository(tagsDataDao)
    }

    fun getAllTags(): LiveData<MutableList<TagsData>> {
        isLoading.value = true
        tagsService?.getAllTags()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val networkData = response.body()?.data
                networkData?.forEachIndexed { _, element ->
                    scope.launch(Dispatchers.IO) { repository.insertTags(element) }
                }
            } else {
                val responseError = response.errorBody()
                if (responseError != null) {
                    val errorBody = String(responseError.bytes())
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    apiResponse.postValue(gson.message)
                }
            }
        })
        { throwable -> apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage)) })
        isLoading.value = false
        return repository.allTags
    }

    fun deleteTagByName(tagName: String): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        isLoading.value = true
        tagsService?.deleteTagByName(tagName)?.enqueue(retrofitCallback({ response ->
            if (response.code() == 204 || response.code() == 200) {
                scope.async(Dispatchers.IO) {
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
}