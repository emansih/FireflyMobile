package xyz.hisname.fireflyiii.repository.attachment

import com.squareup.moshi.Moshi
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Okio
import xyz.hisname.fireflyiii.BuildConfig
import xyz.hisname.fireflyiii.data.local.dao.AttachmentDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.AttachmentService
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import java.io.File
import java.io.InputStream
import java.net.URLConnection


class AttachmentRepository(private val attachmentDao: AttachmentDataDao,
                           private val attachmentService: AttachmentService) {


    suspend fun insertAttachmentInfo(attachment: AttachmentData) = attachmentDao.insert(attachment)

    suspend fun getAttachmentFromJournalId(journalId: Long) = attachmentDao.getAttachmentFromJournalId(journalId)

    suspend fun uploadFile(objectId: Long, fileName: String, tempDir: String,
                           inputStream: InputStream?, attachableType: AttachableType) {
        val tempFileUri = tempDir(inputStream, tempDir, fileName)
        val fileTypeMap = URLConnection.guessContentTypeFromName(fileName)
        val requestFile = RequestBody.create(MediaType.parse(fileTypeMap), tempFileUri)
        val storeAttachment = attachmentService.storeAttachment(fileName, attachableType.toString(),
                objectId, fileName, "File uploaded by " + BuildConfig.APPLICATION_ID)
        val responseBody = storeAttachment.body()
        if (responseBody != null && storeAttachment.code() == 200) {
            insertAttachmentInfo(responseBody.data)
            val upload = attachmentService.uploadFile(responseBody.data.attachmentId, requestFile)
            if(upload.code() != 204) {
                val responseErrorBody = upload.errorBody()
                if(responseErrorBody != null) {
                    val errorMessage = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(responseErrorBody.source())
                    errorMessage?.exception?.let {
                        throw Exception("Unable to add $fileName as there is a file limit on server")
                    }
                    errorMessage?.message?.let {
                        throw Exception(errorMessage.message)
                    }
                }
            }
        } else {
            throw Exception(storeAttachment.message())
        }
        tempFileUri.delete()
    }

    suspend fun downloadOrOpenAttachment(fileDownloadUrl: String, fileLocation: File): File{
        if(fileLocation.exists()){
            return fileLocation
        }
        try {
            val networkCall = attachmentService.downloadFile(fileDownloadUrl)
            val responseBody = networkCall.body()
            if(responseBody != null){
                downloadFile(responseBody, fileLocation)
                return fileLocation
            } else {
                throw Exception("Unable to download file")
            }
        } catch (exception: Exception){
            throw Exception(exception.localizedMessage)
        }
    }

    private fun downloadFile(responseBody: ResponseBody, file: File){
        val source = Okio.buffer(Okio.source(responseBody.byteStream()))
        val sink = Okio.buffer(Okio.sink(file))
        source.use { input ->
            sink.use { output ->
                output.writeAll(input)
            }
        }
    }

    private fun tempDir(stream: InputStream?, tempDir: String, fileName: String): File {
        Okio.source(stream).use {
            a -> Okio.buffer(Okio.sink(File(tempDir + File.pathSeparator + fileName))).use {
            b -> b.writeAll(a)
        }
        }
        return File(tempDir + File.pathSeparator + fileName)
    }
}