package xyz.hisname.fireflyiii.repository.attachment

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import okio.Okio
import xyz.hisname.fireflyiii.data.remote.firefly.api.AttachmentService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.util.network.NetworkErrors
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import xyz.hisname.fireflyiii.util.openFile
import java.io.File

class AttachmentViewModel(application: Application): BaseViewModel(application) {

    private val attachmentService by lazy { genericService().create(AttachmentService::class.java) }
    private val viewModelContext by lazy { getApplication() as Context }
    val isDownloaded: MutableLiveData<Boolean> = MutableLiveData()

    fun downloadAttachment(attachmentData: AttachmentData): LiveData<File> {
        val fileDownloadUrl = attachmentData.attachmentAttributes?.download_uri
        val fileName = attachmentData.attachmentAttributes?.filename ?: ""
        val downloadedFile: MutableLiveData<File> = MutableLiveData()
        val fileToOpen = File(viewModelContext.getExternalFilesDir(null).toString() + File.separator + fileName)
        if(fileToOpen.exists()){
            viewModelContext.openFile(fileToOpen)
            isDownloaded.value = true
            isLoading.value = false
            downloadedFile.postValue(fileToOpen)
        } else {
            attachmentService?.downloadFile(fileDownloadUrl)?.enqueue(retrofitCallback({ downloadResponse ->
                val fileResponse = downloadResponse.body()
                if (fileResponse != null) {
                    viewModelScope.launch(Dispatchers.IO){
                        downloadProgress(fileResponse, fileToOpen)
                    }.invokeOnCompletion {
                        isDownloaded.postValue(true)
                        isLoading.postValue(false)
                        downloadedFile.postValue(fileToOpen)
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
        return downloadedFile
    }

    private fun downloadProgress(responseBody: ResponseBody?, file: File){
        // Double bangs is bad
        val source = Okio.buffer(Okio.source(responseBody!!.byteStream()))
        val sink = Okio.buffer(Okio.sink(file))
        source.use { input ->
            sink.use { output ->
                output.writeAll(input)
            }
        }
    }
}