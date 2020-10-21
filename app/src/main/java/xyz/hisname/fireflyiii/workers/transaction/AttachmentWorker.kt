package xyz.hisname.fireflyiii.workers.transaction

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import androidx.work.*
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.iconics.utils.toAndroidIconCompat
import okhttp3.MediaType
import okhttp3.RequestBody
import xyz.hisname.fireflyiii.BuildConfig
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.AttachmentService
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.showNotification
import xyz.hisname.fireflyiii.workers.BaseWorker
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.UnknownHostException
import java.time.Duration

class AttachmentWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters)  {

    private val channelName by lazy { context.resources.getString(R.string.transaction) }
    private val channelIcon = R.drawable.ic_refresh

    companion object {
        fun initWorker(fileUri: ArrayList<Uri>, transactionJournalId: Long, context: Context){
            val dataBuilder = Data.Builder()
            val uriArray = arrayListOf<String>()
            fileUri.forEach { uri ->
                uriArray.add(uri.toString())
            }
            val appPref = AppPref(PreferenceManager.getDefaultSharedPreferences(context))
            val delay = appPref.workManagerDelay
            val battery = appPref.workManagerLowBattery
            val networkType = appPref.workManagerNetworkType
            val requireCharging = appPref.workManagerRequireCharging
            val attachmentTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("add_attachment_tag_$transactionJournalId").get()
            if(attachmentTag == null || attachmentTag.size == 0){
                val attachmentWork = PeriodicWorkRequestBuilder<AttachmentWorker>(Duration.ofMinutes(delay))
                        .setInputData(dataBuilder.putLong("transactionJournalId" ,transactionJournalId).build())
                        .setInputData(dataBuilder.putStringArray("fileUri", uriArray.toTypedArray()).build())
                        .addTag("add_attachment_tag_$transactionJournalId")
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(networkType)
                                .setRequiresBatteryNotLow(battery)
                                .setRequiresCharging(requireCharging)
                                .build())
                        .build()
                WorkManager.getInstance(context).enqueue(attachmentWork)
            }
        }

        fun cancelWorker(transactionJournalId: Long, context: Context){
            WorkManager.getInstance(context).cancelAllWorkByTag("add_attachment_tag_$transactionJournalId")
        }
    }

    override suspend fun doWork(): Result {
        val transactionJournalId = inputData.getLong("transactionJournalId", 0)
        val fileArray = inputData.getStringArray("fileUri")
        val service = genericService?.create(AttachmentService::class.java)
        if(!fileArray.isNullOrEmpty()){
            fileArray.forEachIndexed { index, fileToUpload ->
                val fileName = FileUtils.getFileName(context, fileToUpload.toUri()) ?: ""
                val requestFile = RequestBody.create(MediaType.parse(FileUtils.getMimeType(context, fileToUpload.toUri()) ?: ""),
                        copyFile(context, fileToUpload.toUri(), fileName))
                try {
                    val storeAttachment = service?.storeAttachment(fileName, "TransactionJournal", transactionJournalId, fileName,
                            "File uploaded by " + BuildConfig.APPLICATION_ID)
                    val responseBody = storeAttachment?.body()
                    if (responseBody != null && storeAttachment.code() == 200) {
                        val upload = service.uploadFile(responseBody.data.attachmentId, requestFile)
                        ("file://" + context.filesDir.path + "/" + fileName).toUri().toFile().delete()
                        if(upload.code() == 204) {
                            context.showNotification("$fileName uploaded", "", channelIcon)
                        } else {
                            val retryIcon = IconicsDrawable(context).apply {
                                icon = GoogleMaterial.Icon.gmd_refresh
                                sizeDp = 24
                            }.toAndroidIconCompat()
                            context.showNotification("$fileName upload failed",
                                    upload.message(), channelIcon)
                        }
                    } else {
                        ("file://" + context.filesDir.path + "/" + fileName).toUri().toFile().delete()
                        context.showNotification("$fileName upload failed", storeAttachment?.message() ?: "", channelIcon)
                    }
                    // Last element in the array. Cancel work
                    if(fileArray.lastIndex == index){
                        cancelWorker(transactionJournalId, context)
                    }
                } catch (unknownHost: UnknownHostException){
                   // Don't cancel work here
                } catch (exception: Exception){
                    // Only cancel work here
                    cancelWorker(transactionJournalId, context)
                    ("file://" + context.filesDir.path + "/" + fileName).toUri().toFile().delete()
                    val throwMessage = exception.message
                    if(throwMessage != null && throwMessage.startsWith("Write error: ssl=")){
                        context.showNotification("Upload Error",
                                "Unable to add attachment to transaction as there is a file limit on server side", channelIcon)
                    } else {
                        context.showNotification("Upload Error",
                                exception.localizedMessage, channelIcon)
                    }
                }
            }
        }
        // Will always return success
        return Result.success()
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