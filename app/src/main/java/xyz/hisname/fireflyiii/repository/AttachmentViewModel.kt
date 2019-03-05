package xyz.hisname.fireflyiii.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.remote.api.AttachmentService
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.network.NetworkErrors
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class AttachmentViewModel(application: Application): BaseViewModel(application) {

    private val attachmentService by lazy { genericService()?.create(AttachmentService::class.java) }

    fun downloadAttachment(fileDownloadUrl: String, fileName: String): LiveData<Boolean> {
        val isDownloaded: MutableLiveData<Boolean> = MutableLiveData()
        var writtenToDisk = false
        attachmentService?.downloadFile(fileDownloadUrl)?.enqueue(retrofitCallback({ downloadResponse ->
            val fileResponse = downloadResponse.body()
            if (fileResponse != null) {
                scope.launch(Dispatchers.IO) {
                    writtenToDisk = FileUtils.writeResponseToDisk(fileResponse, fileName)
                }.invokeOnCompletion {
                    isDownloaded.value = writtenToDisk
                    isLoading.value =  false
                }
            } else {
                isLoading.value = false
                isDownloaded.value = false
            }
        })
        { throwable ->
            isDownloaded.value = false
            isLoading.value = false
            apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage))
        })
        return isDownloaded
    }
}