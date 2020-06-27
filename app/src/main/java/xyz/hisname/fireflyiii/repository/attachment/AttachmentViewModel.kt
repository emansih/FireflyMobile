package xyz.hisname.fireflyiii.repository.attachment

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import xyz.hisname.fireflyiii.data.remote.firefly.api.AttachmentService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.network.NetworkErrors
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import xyz.hisname.fireflyiii.util.openFile
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.pow
import kotlin.math.roundToInt

class AttachmentViewModel(application: Application): BaseViewModel(application) {

    private val attachmentService by lazy { genericService()?.create(AttachmentService::class.java) }
    private val viewModelContext by lazy { getApplication() as Context }
    private var totalFileSize: Int = 0
    val progressListener = MutableLiveData<Int>()

    fun downloadAttachment(attachmentData: AttachmentData): LiveData<Boolean> {
        val isDownloaded: MutableLiveData<Boolean> = MutableLiveData()
        val fileDownloadUrl = attachmentData.attachmentAttributes?.download_uri
        val fileName = attachmentData.attachmentAttributes?.filename ?: ""
        val fileToOpen = File("${FileUtils().folderDirectory(getApplication())}/$fileName")
        totalFileSize = attachmentData.attachmentAttributes?.size ?: 0
        if(fileToOpen.exists()){
            progressListener.postValue(100)
            viewModelContext.openFile(fileName)
            isDownloaded.value = true
            isLoading.value = false
        } else {
            progressListener.postValue(0)
            attachmentService?.downloadFile(fileDownloadUrl)?.enqueue(retrofitCallback({ downloadResponse ->
                val fileResponse = downloadResponse.body()
                if (fileResponse != null) {
                    viewModelScope.launch(Dispatchers.IO){
                        downloadProgress(fileResponse, fileToOpen)
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


    private fun downloadProgress(responseBody: ResponseBody?, file: File){
        var count: Int
        val data = ByteArray(4096)
        val fileSize = responseBody?.contentLength() ?: 0
        val bufferredInputStream = BufferedInputStream(responseBody?.byteStream(), 8192)
        var total = 0L
        val startTime = System.currentTimeMillis()
        var timeCount = 1
        val output = FileOutputStream(file)
        while (bufferredInputStream.read(data).also { count = it } != -1) {
            total += count
            totalFileSize = (fileSize / 1024.0.pow(2.0)).toInt()
            val progress = (total * 100 / fileSize).toInt()
            val currentTime = System.currentTimeMillis() - startTime

            if (currentTime > 1000 * timeCount) {
                progressListener.postValue(progress)
                timeCount++
            }
            output.write(data, 0, count)
        }
        output.flush()
        output.close()
        progressListener.postValue(100)
        bufferredInputStream.close()
    }
}