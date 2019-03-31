package xyz.hisname.fireflyiii.repository.account

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.AccountsDataDao
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData

class AccountRepository(private val accountDao: AccountsDataDao){

    val allAccounts = accountDao.getAllAccounts()
    val assetAccount = accountDao.getAccountByType("asset")
    val expenseAccount = accountDao.getAccountByType("expense")
    val revenueAccount = accountDao.getAccountByType("revenue")
    val liabilityAccount = accountDao.getAccountByType("liabilities")

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertAccount(account: AccountData){
        accountDao.insert(account)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun retrieveAccountById(accountId: Long): MutableList<AccountData>{
        return accountDao.getAccountById(accountId)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAccountById(accountId: Long): Int{
        return accountDao.deleteAccountById(accountId)
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun retrieveAccountByName(accountName: String): MutableList<AccountData>{
        return accountDao.getAccountByName(accountName)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun retrieveAccountWithCurrencyCodeAndNetworth(currencyCode: String): MutableList<AccountData>{
        return accountDao.getAccountsWithNetworthAndCurrency(true, currencyCode)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAccountByType(accountType: String): Int = accountDao.deleteAccountByType(accountType)
}