package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData

@Dao
abstract class AccountsDataDao: BaseDao<AccountData> {

    @Query("SELECT * FROM accounts WHERE name =:accountName AND type =:accountType")
    abstract suspend fun getAccountByNameAndType(accountName: String, accountType: String): AccountData

    @Query("SELECT * FROM accounts WHERE type =:accountType")
    abstract suspend fun getAccountsByType(accountType: String): List<AccountData>

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

    @Query("SELECT * FROM accounts WHERE name LIKE :name AND type =:type")
    abstract suspend fun searchAccountDataByNameAndType(type: String, name: String): List<AccountData>

    @Query("SELECT COUNT(*) FROM accounts WHERE name LIKE :name AND type =:type")
    abstract suspend fun searchAccountDataByNameAndTypeCount(type: String, name: String): Int
}