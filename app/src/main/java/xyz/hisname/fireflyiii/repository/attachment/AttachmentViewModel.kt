package xyz.hisname.fireflyiii.repository.attachment

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.remote.firefly.api.AttachmentService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.checkMd5Hash
import xyz.hisname.fireflyiii.util.network.NetworkErrors
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import xyz.hisname.fireflyiii.util.openFile
import java.io.File

class AttachmentViewModel(application: Application): BaseViewModel(application) {

    private val attachmentService by lazy { genericService()?.create(AttachmentService::class.java) }
    private val viewModelContext by lazy { getApplication() as Context }


    fun downloadAttachment(attachmentData: AttachmentData): LiveData<Boolean> {
        val isDownloaded: MutableLiveData<Boolean> = MutableLiveData()
        val fileDownloadUrl = attachmentData.attachmentAttributes?.download_uri
        val fileName = attachmentData.attachmentAttributes?.filename ?: ""
        isLoading.value = true
        val fileToOpen = File("${FileUtils().folderDirectory}/$fileName")
        // Check file integrity before opening
        if(fileToOpen.checkMd5Hash(attachmentData.attachmentAttributes?.md5 ?: "")){
            viewModelContext.openFile(fileName)
            isDownloaded.value = true
            isLoading.value = false
        } else {
            if(fileToOpen.exists()){
                fileToOpen.delete()
            }
            attachmentService?.downloadFile(fileDownloadUrl)?.enqueue(retrofitCallback({ downloadResponse ->
                val fileResponse = downloadResponse.body()
                if (fileResponse != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        FileUtils.writeResponseToDisk(fileResponse, fileName)
                    }.invokeOnCompletion {
                        isDownloaded.postValue(true)
                        isLoading.postValue(false)
                        viewModelContext.openFile(fileName)
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
        }
        return isDownloaded
    }
}