package xyz.hisname.fireflyiii.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.remote.api.AttachmentService
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.network.NetworkErrors
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class AttachmentViewModel(application: Application): BaseViewModel(application) {

    private val attachmentService by lazy { genericService()?.create(AttachmentService::class.java) }

    fun downloadAttachment(attachmentData: AttachmentData): LiveData<Boolean> {
        val isDownloaded: MutableLiveData<Boolean> = MutableLiveData()
        val fileDownloadUrl = attachmentData.attributes.download_uri
        val fileName = attachmentData.attributes.filename
        isLoading.value = true
        if(!FileUtils.openFile(getApplication(), fileName)) {
            attachmentService?.downloadFile(fileDownloadUrl)?.enqueue(retrofitCallback({ downloadResponse ->
                val fileResponse = downloadResponse.body()
                if (fileResponse != null) {
                    scope.launch(Dispatchers.IO) {
                        FileUtils.writeResponseToDisk(fileResponse, fileName)
                    }.invokeOnCompletion {
                        isDownloaded.postValue(true)
                        isLoading.postValue(false)
                        FileUtils.openFile(getApplication(), fileName)
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
        } else {
            isDownloaded.value = true
            isLoading.value = false
        }
        return isDownloaded
    }
}