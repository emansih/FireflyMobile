package xyz.hisname.fireflyiii.workers.transaction

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.RequestBody
import xyz.hisname.fireflyiii.BuildConfig
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.AttachmentService
import xyz.hisname.fireflyiii.ui.notifications.displayNotification
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import xyz.hisname.fireflyiii.workers.BaseWorker
import java.io.File

class AttachmentWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters)  {

    private val channelName by lazy { context.resources.getString(R.string.transaction) }
    private val channelIcon = R.drawable.ic_refresh

    companion object {
        fun initWorker(fileUri: Uri?, transactionJournalId: Long){
            val dataBuilder = Data.Builder()
            val workBuilder = OneTimeWorkRequest
                    .Builder(AttachmentWorker::class.java)
                    .addTag("attachment_worker")
                    .setInputData(dataBuilder.putLong("transactionJournalId" ,transactionJournalId).build())
                    .setInputData(dataBuilder.putString("fileUri", fileUri.toString()).build())
                    .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build()
            WorkManager.getInstance().enqueue(workBuilder)
        }
    }

    override suspend fun doWork(): Result {
        val transactionJournalId = inputData.getLong("transactionJournalId", 0)
        val fileUri = inputData.getString("fileUri")?.toUri() ?: Uri.EMPTY
        val service = genericService?.create(AttachmentService::class.java)
        val filePath = FileUtils.getPathFromUri(context, fileUri)
        val fileObject = File(filePath)
        val requestFile = RequestBody.create(MediaType.parse(FileUtils.getMimeType(context, fileUri) ?: ""), fileObject)
        val fileName = FileUtils.getFileName(context, fileUri) ?: ""
        var transactionId = 0L
        withContext(Dispatchers.IO){
            val database = AppDatabase.getInstance(context).transactionDataDao()
            transactionId = database.getJournalIdFromTransactionId(transactionJournalId)
        }
        service?.storeAttachment(fileName, "Transaction", transactionId, fileName,
                "File uploaded by " + BuildConfig.APPLICATION_ID)?.enqueue(retrofitCallback({ response ->
            val responseBody = response.body()
            if (response.code() == 200 && responseBody != null) {
                service.uploadFile(responseBody.data.attachmentId, requestFile).enqueue(retrofitCallback({ uploadFileResponse ->
                    if(uploadFileResponse.code() == 204) {
                        context.displayNotification("File uploaded", channelName,
                                Constants.TRANSACTION_CHANNEL, channelIcon)
                    } else {
                        context.displayNotification(uploadFileResponse.message(), channelName,
                                Constants.TRANSACTION_CHANNEL, channelIcon)
                    }
                })
                { throwable ->
                    val throwMessage = throwable.message
                    if(throwMessage != null && throwMessage.startsWith("Write error: ssl=")){
                        context.displayNotification("Unable to add attachment to transaction as " +
                                "there is a file limit on server side", channelName,
                                Constants.TRANSACTION_CHANNEL, channelIcon)
                    } else {
                        context.displayNotification(throwable.localizedMessage, channelName,
                                Constants.TRANSACTION_CHANNEL, channelIcon)
                    }
                })
            }
        })
        { throwable ->
            context.displayNotification(throwable.localizedMessage, channelName,
                    Constants.TRANSACTION_CHANNEL, channelIcon)
        })
        // Since this is async, this *WILL* always result in success
        return Result.success()
    }
}