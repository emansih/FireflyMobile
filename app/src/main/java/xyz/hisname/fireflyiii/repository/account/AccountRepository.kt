package xyz.hisname.fireflyiii.repository.account

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.AccountsDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.models.accounts.AccountsModel
import xyz.hisname.fireflyiii.workers.account.DeleteAccountWorker

@Suppress("RedundantSuspendModifier")
class AccountRepository(private val accountDao: AccountsDataDao,
                        private val accountsService: AccountsService?){

    private lateinit var apiResponse: String

    suspend fun insertAccount(account: AccountData){
        accountDao.insert(account)
    }

    suspend fun getAccountByType(accountType: String): MutableList<AccountData>{
        loadRemoteData(accountType)
        return accountDao.getAccountByType(accountType)
    }

    suspend fun retrieveAccountById(accountId: Long) = accountDao.getAccountById(accountId)

    suspend fun deleteAccountById(accountId: Long, shouldUseWorker: Boolean = false): Boolean {
        var networkResponse: Response<AccountsModel>? = null
        withContext(Dispatchers.IO){
            networkResponse = accountsService?.deleteAccountById(accountId)
        }
        return if (networkResponse?.code() == 204 || networkResponse?.code() == 200){
            withContext(Dispatchers.IO) {
                accountDao.deleteAccountById(accountId)
            }
            true
        } else {
            if(shouldUseWorker){
                DeleteAccountWorker.deleteWorker(accountId)
            }
            false
        }
    }

    suspend fun retrieveAccountByName(accountName: String) = accountDao.getAccountByName(accountName)

    suspend fun retrieveAccountWithCurrencyCodeAndNetworth(currencyCode: String): Double {
        loadRemoteData("all")
        return accountDao.getAccountsWithNetworthAndCurrency(currencyCode = currencyCode)
    }

    suspend fun deleteAccountByType(accountType: String): Int = accountDao.deleteAccountByType(accountType)

    private suspend fun loadRemoteData(accountType: String){
        var networkCall: Response<AccountsModel>? = null
        val accountData: MutableList<AccountData> = arrayListOf()
        try {
            withContext(Dispatchers.IO) {
                withContext(Dispatchers.IO) {
                    networkCall = accountsService?.getPaginatedAccountType(accountType, 1)
                }
                accountData.addAll(networkCall?.body()?.data?.toMutableList() ?: arrayListOf())
            }
            val responseBody = networkCall?.body()
            apiResponse = networkCall?.message() ?: ""
            if (responseBody != null && networkCall?.isSuccessful != false) {
                val pagination = responseBody.meta.pagination
                if (pagination.total_pages != pagination.current_page) {
                    withContext(Dispatchers.IO) {
                        for (items in 2..pagination.total_pages) {
                            accountData.addAll(
                                    accountsService?.getPaginatedAccountType(accountType, items)?.body()?.data?.toMutableList() ?: arrayListOf()
                            )
                        }
                    }
                }
                withContext(Dispatchers.IO) {
                    deleteAccountByType(accountType)
                }
                withContext(Dispatchers.IO) {
                    accountData.forEachIndexed { _, data ->
                        accountDao.insert(data)
                    }
                }
            }
        } catch (exception: Exception){ }

    }
}