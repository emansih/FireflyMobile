package xyz.hisname.fireflyiii.util

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import androidx.core.net.toUri
import java.io.*

// https://gist.github.com/micer/ae5de2984dbbdb386dd262782cfdb39c
class FileUtils {

    fun folderDirectory(context: Context): File{
        return File(context.getExternalFilesDir(null), "FireflyIIIMobile")
    }

    companion object {

        fun copyFile(sourceDestination: File, endDestination: File){
            sourceDestination.copyTo(endDestination, true)
        }

        fun readFileContent(filePath: File): String{
            val file = File(filePath.toString())
            val fileContent  = StringBuilder()
            try {
                val br = BufferedReader(FileReader(file))
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    fileContent.append(line)
                    fileContent.append('\n')
                }
                br.close()
            } catch (e: IOException) { }
            return fileContent.toString()
        }

        @Throws(IOException::class)
        fun saveCaFile(fileUri: Uri, context: Context){
            val filePath = ("file://$fileUri").toUri().toFile()
            val fileDestination = "user_custom.pem"
            if(readFileContent(filePath).isBlank()){
                throw IOException("Your CA File is empty!")
            }
            val br = BufferedReader(FileReader(filePath))
            var line: String?
            context.deleteFile("user_custom.pem")
            while (br.readLine().also { line = it + "\n" } != null) {
                context.openFileOutput(fileDestination, Context.MODE_APPEND).use {
                    it.write(line?.toByteArray())
                }
            }
            br.close()
        }

        fun getPathFromUri(context: Context, uri: Uri): String? {
            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (uri.isExternalStorageDocument()) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]

                    if ("primary".equals(type, ignoreCase = true)) {
                        val path = StringBuilder()
                        path.append(Environment.getExternalStorageDirectory())
                        path.append("/")
                        path.append(split[1])
                        return path.toString()
                    }
                } else if (uri.isDownloadsDocument()) {
                    val id = DocumentsContract.getDocumentId(uri)
                    if (id.isNotEmpty()) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:", "");
                        }
                        return try {
                            val contentUri = ContentUris.withAppendedId(
                                    Uri.parse("content://downloads/public_downloads"), id.toLong())
                            getDataColumn(context, contentUri, null, null)
                        } catch (e: NumberFormatException) {
                            null
                        }
                    }
                } else if (uri.isMediaDocument()) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]

                    lateinit var contentUri: Uri
                    when (type) {
                        "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }

                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])

                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }// MediaProvider
                // DownloadsProvider
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {

                // Return the remote address
                return if (uri.isGooglePhotosUri()) uri.lastPathSegment else getDataColumn(context, uri, null, null)

            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }// File
            // MediaStore (and general)

            return null
        }

        fun getFileName(context: Context, uri: Uri): String?{
            var result: String? = null
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor.use { curse ->
                    if (curse != null && curse.moveToFirst()) {
                        result = curse.getString(curse.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result?.lastIndexOf('/')
                if (cut != -1) {
                    result = result?.substring(cut?.plus(1) ?: 0)
                }
            }
            return result
        }

        fun getMimeType(context: Context, uri: Uri): String?{
            return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                context.contentResolver.getType(uri)
            } else {
                val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                        .toString())
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        fileExtension.toLowerCase())
            }
        }

        private fun Uri?.isMediaDocument(): Boolean {
            return "com.android.providers.media.documents" == this?.authority
        }

        private fun Uri?.isExternalStorageDocument(): Boolean {
            return "com.android.externalstorage.documents" == this?.authority
        }

        private fun Uri?.isDownloadsDocument(): Boolean {
            return "com.android.providers.downloads.documents" == this?.authority
        }

        private fun getDataColumn(context: Context, uri: Uri, selection: String?,
                                  selectionArgs: Array<String>?): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)
            try {
                cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        private fun Uri?.isGooglePhotosUri(): Boolean {
            return "com.google.android.apps.photos.content" == this?.authority
        }
    }
}

fun Context.openFile(fileName: String): Boolean{
    val fileToOpen = File("${FileUtils().folderDirectory(this)}/$fileName")
    return if(fileToOpen.exists() && !fileToOpen.isDirectory){
        val fileIntent = Intent(Intent.ACTION_VIEW)
        fileIntent.setDataAndType(Uri.parse("${FileUtils().folderDirectory(this)}/$fileName"),
                FileUtils.getMimeType(this, "${FileUtils().folderDirectory(this)}/$fileName".toUri()))
        val openFileIntent = Intent.createChooser(fileIntent, "Open File")
        openFileIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        this.startActivity(openFileIntent)
        true
    } else {
        false
    }
}