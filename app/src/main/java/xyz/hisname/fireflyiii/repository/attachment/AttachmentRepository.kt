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

package xyz.hisname.fireflyiii.repository.attachment

import com.squareup.moshi.Moshi
import okhttp3.MediaType
import okhttp3.RequestBody
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

    private fun tempDir(stream: InputStream?, tempDir: String, fileName: String): File {
        Okio.source(stream).use {
            a -> Okio.buffer(Okio.sink(File(tempDir + File.pathSeparator + fileName))).use {
            b -> b.writeAll(a)
        }
        }
        return File(tempDir + File.pathSeparator + fileName)
    }
}