package xyz.hisname.fireflyiii.repository.account

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.AccountsDataDao
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData

class AccountRepository(private val accountDao: AccountsDataDao){

    val allAccounts = accountDao.getAllAccounts()
    val assetAccount = accountDao.getAccountByType("Asset account")
    val expenseAccount = accountDao.getAccountByType("Expense account")
    val revenueAccount = accountDao.getAccountByType("Revenue account")
    val liabilityAccount = accountDao.getAccountByType("Liability account")

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertAccount(account: AccountData){
        accountDao.insert(account)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun retrieveAccountByType(accountType: String): MutableList<AccountData>{
        return accountDao.getAccountsByType(accountType)
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

}