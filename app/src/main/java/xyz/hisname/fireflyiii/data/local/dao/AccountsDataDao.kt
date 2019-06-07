package xyz.hisname.fireflyiii.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData

@Dao
abstract class AccountsDataDao: BaseDao<AccountData> {

    @Query("SELECT * FROM accounts")
    abstract fun getAllAccounts(): LiveData<MutableList<AccountData>>

    @Query("SELECT * FROM accounts WHERE name =:accountName")
    abstract fun getAccountByName(accountName: String): MutableList<AccountData>

    @Query("SELECT * FROM accounts WHERE type =:accountType")
    abstract fun getAccountByType(accountType: String): MutableList<AccountData>

    @Query("SELECT * FROM accounts WHERE type =:accountType")
    abstract fun getAccountsByType(accountType: String): MutableList<AccountData>

    @Query("SELECT * FROM accounts WHERE accountId =:accountId")
    abstract fun getAccountById(accountId: Long): MutableList<AccountData>

    @Query("DELETE FROM accounts WHERE accountId = :accountId")
    abstract fun deleteAccountById(accountId: Long): Int

    @Query("SELECT sum(current_balance) as someValue FROM accounts WHERE " +
            "currency_code =:currencyCode AND include_net_worth =:networth")
    abstract fun getAccountsWithNetworthAndCurrency(networth: Boolean = true,
                                                    currencyCode: String): Double

    @Query("DELETE FROM accounts WHERE type =:accountType")
    abstract fun deleteAccountByType(accountType: String): Int
}