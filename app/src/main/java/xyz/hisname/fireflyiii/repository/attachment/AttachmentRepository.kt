package xyz.hisname.fireflyiii.repository.attachment

import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.core.net.toFile
import androidx.core.net.toUri
import okhttp3.MediaType
import okhttp3.RequestBody
import xyz.hisname.fireflyiii.BuildConfig
import xyz.hisname.fireflyiii.data.local.dao.AttachmentDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.AttachmentService
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.util.FileUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.jvm.Throws

@Suppress("RedundantSuspendModifier")
@WorkerThread
class AttachmentRepository(private val attachmentDao: AttachmentDataDao,
                           private val attachmentService: AttachmentService) {

    suspend fun insertAttachmentInfo(attachment: AttachmentData) = attachmentDao.insert(attachment)

    suspend fun getAttachmentFromJournalId(journalId: Long) = attachmentDao.getAttachmentFromJournalId(journalId)

    @Throws(Exception::class)
    // Bad idea to use context in this method
    suspend fun uploadFile(context: Context, transactionJournalId: Long, fileUri: ArrayList<Uri>): ArrayList<String>{
        val fileStatus = arrayListOf<String>()
        fileUri.forEach { fileToUpload ->
            val fileName = FileUtils.getFileName(context, fileToUpload) ?: ""
            val requestFile = RequestBody.create(MediaType.parse(FileUtils.getMimeType(context,
                    fileToUpload) ?: ""), copyFile(context, fileToUpload, fileName))
            val storeAttachment = attachmentService.storeAttachment(fileName, "TransactionJournal",
                    transactionJournalId, fileName,
                    "File uploaded by " + BuildConfig.APPLICATION_ID)
            val responseBody = storeAttachment.body()
            if (responseBody != null && storeAttachment.code() == 200) {
                val upload = attachmentService.uploadFile(responseBody.data.attachmentId, requestFile)
                File(context.filesDir.path + "/" + fileName).delete()
                if(upload.code() == 204) {
                    // success
                } else {
                    val responseErrorBody = upload.errorBody()
                    if(responseErrorBody != null) {
                        val errorMessage = String(responseErrorBody.bytes())
                        if(errorMessage.contains("Write error: ssl=")){
                            fileStatus.add("Unable to add $fileName as there is a file limit on server")
                        } else {
                            fileStatus.add(errorMessage)
                        }
                    }
                }
            } else {
                File(context.filesDir.path + "/" + fileName).delete()
                fileStatus.add(storeAttachment.message())
            }
        }
        return fileStatus
    }

    private fun copyFile(context: Context, fileUri: Uri, name: String): File {
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
        return File(context.filesDir.path + "/" + name)
    }
}