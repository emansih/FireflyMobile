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

package xyz.hisname.fireflyiii.data.local.dao

import android.database.Cursor
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData

@Dao
abstract class AccountsDataDao: BaseDao<AccountData> {

    @Query("SELECT * FROM accounts WHERE name =:accountName AND type =:accountType")
    abstract suspend fun getAccountByNameAndType(accountName: String, accountType: String): AccountData

    @Query("SELECT * FROM accounts WHERE type =:accountType ORDER BY accountId ASC")
    abstract fun getAccountsByType(accountType: String): PagingSource<Int, AccountData>

    @Query("SELECT * FROM accounts WHERE type =:accountType")
    abstract suspend fun getAccountsListByType(accountType: String): List<AccountData>

    @Query("SELECT COUNT(*) FROM accounts WHERE type =:accountType")
    abstract suspend fun getAccountsByTypeCount(accountType: String): Int

    @Query("SELECT * FROM accounts WHERE accountId =:accountId")
    abstract fun getAccountById(accountId: Long): AccountData

    @Query("DELETE FROM accounts WHERE accountId = :accountId")
    abstract fun deleteAccountById(accountId: Long): Int

    @Query("SELECT sum(current_balance) as someValue FROM accounts WHERE " +
            "currency_code =:currencyCode AND include_net_worth =:networth")
    abstract fun getAccountsWithNetworthAndCurrency(networth: Boolean = true,
                                                    currencyCode: String): Double

    @Query("DELETE FROM accounts WHERE type =:accountType AND isPending IS NOT :isPending")
    abstract suspend fun deleteAccountByType(accountType: String, isPending: Boolean = true): Int

    @Query("DELETE FROM accounts WHERE type =:accountType AND name =:accountName")
    abstract fun deleteAccountByTypeAndName(accountType: String, accountName: String): Int

    @Query("SELECT distinct name FROM accounts WHERE name LIKE :name AND type LIKE :type")
    abstract fun searchAccountByNameAndType(type: String, name: String): List<String>

    @Query("SELECT * FROM accounts WHERE name LIKE :name AND type =:type ORDER BY accountId ASC")
    abstract fun searchAccountDataByNameAndType(type: String, name: String): PagingSource<Int, AccountData>

    @Query("SELECT COUNT(*) FROM accounts WHERE name LIKE :name AND type =:type")
    abstract suspend fun searchAccountDataByNameAndTypeCount(type: String, name: String): Int

    // Send minimal data over the wire instead of the whole data set
    @Query("SELECT accountId, name, current_balance, currency_symbol FROM accounts WHERE type ='asset'")
    abstract fun getAssetAccountCursor(): Cursor

}