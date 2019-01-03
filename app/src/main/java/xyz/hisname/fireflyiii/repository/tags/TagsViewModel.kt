package xyz.hisname.fireflyiii.repository.tags

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.api.TagsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.tags.TagsData
import xyz.hisname.fireflyiii.repository.models.tags.TagsSuccessModel
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
        var tagsData: MutableList<TagsData> = arrayListOf()
        val data: MutableLiveData<MutableList<TagsData>> = MutableLiveData()
        tagsService?.getAllTags()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val networkData = response.body()?.data
                networkData?.forEachIndexed { _, element ->
                    scope.launch(Dispatchers.IO) { repository.insertTags(element) }
                }
                data.postValue(networkData?.toMutableList())
            } else {
                val responseError = response.errorBody()
                if (responseError != null) {
                    val errorBody = String(responseError.bytes())
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    apiResponse.postValue(gson.message)
                }
                scope.async(Dispatchers.IO) {
                    tagsData = repository.allTags()
                }.invokeOnCompletion {
                    data.postValue(tagsData)
                }
            }
        })
        { throwable ->
            scope.async(Dispatchers.IO) {
                tagsData = repository.allTags()
            }.invokeOnCompletion {
                data.postValue(tagsData)
            }
            apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage))
        })
        isLoading.value = false
        return data
    }

    fun addTag(tagName: String, date: String?, description: String?, latitude: String?, longitude: String?,
               zoomLevel: String?): LiveData<ApiResponses<TagsSuccessModel>>{
        val apiResponse: MediatorLiveData<ApiResponses<TagsSuccessModel>> =  MediatorLiveData()
        val apiLiveData: MutableLiveData<ApiResponses<TagsSuccessModel>> = MutableLiveData()
        tagsService?.createNewTag(tagName, date, description, latitude, longitude, zoomLevel)?.enqueue(retrofitCallback({
            response ->
            var errorMessage = ""
            val responseErrorBody = response.errorBody()
            if (responseErrorBody != null) {
                errorMessage = String(responseErrorBody.bytes())
                val gson = Gson().fromJson(errorMessage, ErrorModel::class.java)
                errorMessage = when {
                    gson.errors.longitude != null -> gson.errors.longitude[0]
                    gson.errors.tag != null -> gson.errors.tag[0]
                    gson.errors.latitude != null -> gson.errors.latitude[0]
                    gson.errors.zoomLevel != null -> gson.errors.zoomLevel[0]
                    else -> "Error occurred while saving tag"
                }
            }
            val networkData = response.body()
            if (networkData != null) {
                scope.launch(Dispatchers.IO) { repository.insertTags(networkData.data) }
                apiLiveData.postValue(ApiResponses(response.body()))
            } else {
                apiLiveData.postValue(ApiResponses(errorMessage))
            }
        })
        { throwable ->
            apiResponse.postValue(ApiResponses(throwable))
        })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse
    }

    fun updateTag(tagId: Long, tagName: String, date: String?, description: String?, latitude: String?, longitude: String?,
               zoomLevel: String?): LiveData<ApiResponses<TagsSuccessModel>>{
        val apiResponse: MediatorLiveData<ApiResponses<TagsSuccessModel>> =  MediatorLiveData()
        val apiLiveData: MutableLiveData<ApiResponses<TagsSuccessModel>> = MutableLiveData()
        tagsService?.updateTag(tagId, tagName, date, description, latitude, longitude, zoomLevel)?.enqueue(retrofitCallback({
            response ->
            var errorMessage = ""
            val responseErrorBody = response.errorBody()
            if (responseErrorBody != null) {
                errorMessage = String(responseErrorBody.bytes())
                val gson = Gson().fromJson(errorMessage, ErrorModel::class.java)
                errorMessage = when {
                    gson.errors.longitude != null -> gson.errors.longitude[0]
                    gson.errors.tag != null -> gson.errors.tag[0]
                    gson.errors.latitude != null -> gson.errors.latitude[0]
                    gson.errors.zoomLevel != null -> gson.errors.zoomLevel[0]
                    else -> "Error occurred while updating tag"
                }
            }
            val networkData = response.body()
            if (networkData != null) {
                scope.launch(Dispatchers.IO) { repository.insertTags(networkData.data) }
                apiLiveData.postValue(ApiResponses(response.body()))
            } else {
                apiLiveData.postValue(ApiResponses(errorMessage))
            }
        })
        { throwable ->
            apiResponse.postValue(ApiResponses(throwable))
        })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse
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

    fun getTagById(tagId: Long): LiveData<MutableList<TagsData>>{
        val tagData: MutableLiveData<MutableList<TagsData>> = MutableLiveData()
        var data: MutableList<TagsData> = arrayListOf()
        scope.async(Dispatchers.IO){
            data = repository.retrieveTagById(tagId)
        }.invokeOnCompletion {
            tagData.postValue(data)
        }
        return tagData
    }
}