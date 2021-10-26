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

package xyz.hisname.fireflyiii.util.extension

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import androidx.core.net.toUri
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.util.getUniqueHash
import java.io.File

fun Application.downloadFile(accessToken: String, attachmentData: AttachmentData, fileToOpen: File){
    val downloadManager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val request = DownloadManager.Request(attachmentData.attachmentAttributes.download_url)
    request.addRequestHeader("Authorization", "Bearer $accessToken")
    request.setTitle("Downloading " + attachmentData.attachmentAttributes.filename)
    val sharedPref = this.getSharedPreferences(this.getUniqueHash().toString() + "-user-preferences", Context.MODE_PRIVATE)
    val userPref = AppPref(sharedPref).userDefinedDownloadDirectory
    val userDirectory = if(userPref.isEmpty()){
        File(getExternalFilesDir(null).toString()).toString()
    } else {
        userPref
    }
    request.setDestinationUri(("$userDirectory/$fileToOpen").toUri())
    request.setMimeType(attachmentData.attachmentAttributes.mime)
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
    downloadManager.enqueue(request)
}
