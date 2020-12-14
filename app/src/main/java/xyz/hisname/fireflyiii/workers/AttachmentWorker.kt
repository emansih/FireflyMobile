package xyz.hisname.fireflyiii.workers

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import androidx.work.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.AttachmentService
import xyz.hisname.fireflyiii.repository.attachment.AttachableType
import xyz.hisname.fireflyiii.repository.attachment.AttachmentRepository
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.showNotification

class AttachmentWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters)  {



    companion object {
        fun initWorker(fileUri: ArrayList<Uri>, objectId: Long, context: Context, attachableType: AttachableType){
            val appPref = AppPref(PreferenceManager.getDefaultSharedPreferences(context))
            val battery = appPref.workManagerLowBattery
            val networkType = appPref.workManagerNetworkType
            val requireCharging = appPref.workManagerRequireCharging
            val dataBuilder = Data.Builder()
            val arrayOfRequest = arrayListOf<OneTimeWorkRequest>()
            fileUri.forEach { uri ->
                val attachmentTag =
                        WorkManager.getInstance(context).getWorkInfosByTag("add_attachment_tag_$objectId" + "_$attachableType").get()
                if(attachmentTag == null || attachmentTag.size == 0){
                    val attachmentWork = OneTimeWorkRequestBuilder<AttachmentWorker>()
                            .setInputData(dataBuilder.putLong("objectId", objectId).build())
                            .setInputData(dataBuilder.putString("fileUri", uri.toString()).build())
                            .setInputData(dataBuilder.putString("attachableType", attachableType.name).build())
                            .addTag("add_attachment_tag_$objectId")
                            .setConstraints(Constraints.Builder()
                                    .setRequiredNetworkType(networkType)
                                    .setRequiresBatteryNotLow(battery)
                                    .setRequiresCharging(requireCharging)
                                    .build())
                            .build()
                    arrayOfRequest.add(attachmentWork)
                }
            }
            WorkManager.getInstance(context).beginWith(arrayOfRequest).enqueue()
        }
    }

    override suspend fun doWork(): Result {
        val objectId = inputData.getLong("objectId", 0)
        val fileUri = inputData.getString("fileUri")
        val service = genericService.create(AttachmentService::class.java)
        val attachmentDao = AppDatabase.getInstance(context).attachmentDataDao()
        val attachmentRepository = AttachmentRepository(attachmentDao, service)
        val fileName = FileUtils.getFileName(context, fileUri?.toUri() ?: Uri.EMPTY)
        try {
            val attachableType = AttachableType.valueOf(inputData.getString("attachableType") ?: "")
            attachmentRepository.uploadFile(objectId, fileName ?: "",
                    context.filesDir.path, context.contentResolver.openInputStream(fileUri?.toUri()  ?: Uri.EMPTY),
                    attachableType)
        } catch (exception: Exception){
            context.showNotification("Failed to upload $fileName",
                    exception.localizedMessage, R.drawable.app_icon)
            return Result.failure()
        }
        return Result.success()
    }

}