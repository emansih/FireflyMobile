/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.workers

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
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
        fun initWorker(fileUri: List<Uri>, objectId: Long, context: Context, attachableType: AttachableType): LiveData<List<WorkInfo>> {
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
            val workManager = WorkManager.getInstance(context).beginWith(arrayOfRequest)
            workManager.enqueue()
            return workManager.workInfosLiveData
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
            exception.printStackTrace()
            context.showNotification("Failed to upload $fileName",
                    exception.localizedMessage, R.drawable.app_icon)
            return Result.failure()
        }
        return Result.success()
    }

}