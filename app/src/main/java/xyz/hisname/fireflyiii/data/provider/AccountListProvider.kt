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
import xyz.hisname.fireflyiii.data.local.dao.FireflyUserDatabase
import java.util.*

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
                val accountDao = AppDatabase.getInstance(nonNullContext,
                    FireflyUserDatabase.getInstance(nonNullContext)
                            .fireflyUserDao().getUniqueHash()
                ).accountDataDao()
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