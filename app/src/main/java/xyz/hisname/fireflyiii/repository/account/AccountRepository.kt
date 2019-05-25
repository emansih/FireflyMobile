package xyz.hisname.fireflyiii.repository.account

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.AccountsDataDao
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData

@Suppress("RedundantSuspendModifier")
@WorkerThread
class AccountRepository(private val accountDao: AccountsDataDao){

    val allAccounts = accountDao.getAllAccounts()
    val assetAccount = accountDao.getAccountByType("asset")
    val expenseAccount = accountDao.getAccountByType("expense")
    val revenueAccount = accountDao.getAccountByType("revenue")
    val liabilityAccount = accountDao.getAccountByType("liabilities")

    suspend fun insertAccount(account: AccountData){
        accountDao.insert(account)
    }

    suspend fun getAccountByType(accountType: String) = accountDao.getAccountByType(accountType)

    suspend fun retrieveAccountById(accountId: Long) = accountDao.getAccountById(accountId)

    suspend fun deleteAccountById(accountId: Long) = accountDao.deleteAccountById(accountId)

    suspend fun retrieveAccountByName(accountName: String) = accountDao.getAccountByName(accountName)

    suspend fun retrieveAccountWithCurrencyCodeAndNetworth(currencyCode: String) =
            accountDao.getAccountsWithNetworthAndCurrency(true, currencyCode)

    suspend fun deleteAccountByType(accountType: String): Int = accountDao.deleteAccountByType(accountType)
}