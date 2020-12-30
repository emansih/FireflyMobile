package xyz.hisname.fireflyiii.util.extension

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import androidx.core.net.toUri
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import java.io.File

fun Application.downloadFile(accessToken: String, attachmentData: AttachmentData, fileToOpen: File){
    val downloadManager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val request = DownloadManager.Request(attachmentData.attachmentAttributes.download_uri)
    request.addRequestHeader("Authorization", "Bearer $accessToken")
    request.setTitle("Downloading " + attachmentData.attachmentAttributes.filename)
    request.setDestinationUri(fileToOpen.toUri())
    request.setMimeType(attachmentData.attachmentAttributes.mime)
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
    downloadManager.enqueue(request)
}