package xyz.hisname.fireflyiii.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase

class AccountListProvider: ContentProvider() {

    companion object {
        const val PROVIDER_NAME = "xyz.hisname.fireflyiii.provider"
        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        const val uriCode = 1
    }

    init {
        uriMatcher.addURI(PROVIDER_NAME, "accountList", uriCode)
    }

    override fun onCreate() = true

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?,
                       selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        if (uriMatcher.match(uri) != -1){
            context?.let { nonNullContext ->
                val accountDao = AppDatabase.getInstance(nonNullContext).accountDataDao()
                return accountDao.getAssetAccountCursor()
            }
        }
        return null
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            uriCode -> "vnd.android.cursor.dir/AccountListProvider"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?) = uri

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<out String>?) = 0
}