package xyz.hisname.fireflyiii.util

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import okhttp3.ResponseBody
import java.io.*
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


// https://gist.github.com/micer/ae5de2984dbbdb386dd262782cfdb39c
class FileUtils {

    val folderDirectory by lazy { File(Environment.getExternalStorageDirectory(), "FireflyIIIMobile") }

    companion object {

        fun getPathFromUri(context: Context, uri: Uri): String? {
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
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

        fun writeResponseToDisk(body: ResponseBody, fileName: String): Boolean {
            if(!createDirIfNotExists()){
                return false
            }
            try {
                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null

                try {
                    val fileReader = ByteArray(4096)
                    var fileSizeDownloaded: Long = 0
                    inputStream = body.byteStream()
                    outputStream = FileOutputStream("${FileUtils().folderDirectory}/$fileName")
                    while (true) {
                        val read = inputStream.read(fileReader)
                        if (read == -1) {
                            break
                        }
                        outputStream.write(fileReader, 0, read)
                        fileSizeDownloaded += read.toLong()
                    }

                    outputStream.flush()
                    return true
                } catch (e: IOException) {
                    return false
                } finally {
                    inputStream?.close()
                    outputStream?.close()
                }
            } catch (e: IOException) {
                return false
            }
        }

        private fun createDirIfNotExists(): Boolean{
            var ret = true
            if(!FileUtils().folderDirectory.exists()){
                if(!FileUtils().folderDirectory.mkdir()){
                    ret = false
                }
            }
            return ret
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

// https://github.com/CyanogenMod/android_packages_apps_CMUpdater/blob/5ca1160572df1bab60e271e2f6cfde03d452ffa1/src/com/cyanogenmod/updater/utils/MD5.java
fun File.checkMd5Hash(md5Hash: String): Boolean{
    val digest = try {
        MessageDigest.getInstance("MD5")
    } catch (e: NoSuchAlgorithmException){
        return false
    }
    val inputStream = try {
        FileInputStream(this)
    } catch (e: FileNotFoundException){
        return false
    }
    val buffer = ByteArray(8192)
    val read = 0
    return try {
        while((inputStream.read(buffer)) > 0){
            digest.update(buffer, 0, read)
        }
        val md5sum = digest.digest()
        val bigInt = BigInteger(1, md5sum)
        val output = String.format("%32s", bigInt.toString(16)).replace(' ', '0')
        output.equals(md5Hash,  ignoreCase = true)
    } catch (e: IOException){
        false
    } finally {
        inputStream.close()
    }
}

fun Context.openFile(fileName: String): Boolean{
    val fileToOpen = File("${FileUtils().folderDirectory}/$fileName")
    return if(fileToOpen.exists() && !fileToOpen.isDirectory){
        val fileIntent = Intent(Intent.ACTION_VIEW)
        fileIntent.setDataAndType(Uri.parse("${FileUtils().folderDirectory}/$fileName"),
                FileUtils.getMimeType(this, "${FileUtils().folderDirectory}/$fileName".toUri()))
        val openFileIntent = Intent.createChooser(fileIntent, "Open File")
        this.startActivity(openFileIntent)
        true
    } else {
        false
    }
}