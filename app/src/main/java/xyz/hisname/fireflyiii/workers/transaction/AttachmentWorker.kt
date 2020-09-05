package xyz.hisname.fireflyiii.workers.transaction

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.work.*
import okhttp3.MediaType
import okhttp3.RequestBody
import xyz.hisname.fireflyiii.BuildConfig
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.remote.firefly.api.AttachmentService
import xyz.hisname.fireflyiii.ui.notifications.displayNotification
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.workers.BaseWorker
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream

class AttachmentWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters)  {

    private val channelName by lazy { context.resources.getString(R.string.transaction) }
    private val channelIcon = R.drawable.ic_refresh

    companion object {
        fun initWorker(fileUri: Uri?, transactionJournalId: Long, context: Context){
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
            WorkManager.getInstance(context).enqueue(workBuilder)
        }
    }

    override suspend fun doWork(): Result {
        val transactionJournalId = inputData.getLong("transactionJournalId", 0)
        val fileUri = inputData.getString("fileUri")?.toUri() ?: Uri.EMPTY
        val service = genericService?.create(AttachmentService::class.java)
        val fileName = FileUtils.getFileName(context, fileUri) ?: ""
        val requestFile = RequestBody.create(MediaType.parse(FileUtils.getMimeType(context, fileUri) ?: ""), copyFile(context, fileUri, fileName))
        try {
            val storeAttachment = service?.storeAttachment(fileName, "TransactionJournal", transactionJournalId, fileName,
                    "File uploaded by " + BuildConfig.APPLICATION_ID)
            val responseBody = storeAttachment?.body()
            if (responseBody != null && storeAttachment.code() == 200) {
                val upload = service.uploadFile(responseBody.data.attachmentId, requestFile)
                ("file://" + context.filesDir.path + "/" + fileName).toUri().toFile().delete()
                if(upload.code() == 204) {
                    context.displayNotification("File uploaded", channelName,
                            Constants.TRANSACTION_CHANNEL, channelIcon)
                    return Result.success()
                } else {
                    context.displayNotification(upload.message(), channelName,
                            Constants.TRANSACTION_CHANNEL, channelIcon)
                    return Result.failure()
                }
            } else {
                ("file://" + context.filesDir.path + "/" + fileName).toUri().toFile().delete()
                context.displayNotification(storeAttachment?.message() ?: "", channelName,
                        Constants.TRANSACTION_CHANNEL, channelIcon)
                return Result.failure()
            }
        } catch (exception: Exception){
            ("file://" + context.filesDir.path + "/" + fileName).toUri().toFile().delete()
            val throwMessage = exception.message
            if(throwMessage != null && throwMessage.startsWith("Write error: ssl=")){
                context.displayNotification("Unable to add attachment to transaction as " +
                        "there is a file limit on server side", channelName,
                        Constants.TRANSACTION_CHANNEL, channelIcon)
            } else {
                context.displayNotification(exception.localizedMessage, channelName,
                        Constants.TRANSACTION_CHANNEL, channelIcon)
            }
            return Result.failure()
        }
    }

    private fun copyFile(context: Context, fileUri: Uri, name: String): File{
        var count: Int
        val data = ByteArray(4096)
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val bufferredInputStream = BufferedInputStream(inputStream, 8192)
        val output = FileOutputStream(context.filesDir.toString() + "/" + name)
        while (bufferredInputStream.read(data).also { count = it } != -1) {
            output.write(data, 0, count)
        }
        output.flush()
        output.close()
        bufferredInputStream.close()
        return ("file://" + context.filesDir.path + "/" + name).toUri().toFile()
    }
}