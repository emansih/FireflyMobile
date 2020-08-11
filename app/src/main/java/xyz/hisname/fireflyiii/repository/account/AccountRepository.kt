package xyz.hisname.fireflyiii.repository.account

import android.content.Context
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.data.local.dao.AccountsDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.workers.account.DeleteAccountWorker
import java.security.cert.CertificateException

@Suppress("RedundantSuspendModifier")
class AccountRepository(private val accountDao: AccountsDataDao,
                        private val accountsService: AccountsService?){

    private lateinit var apiResponse: String
    val responseApi: MutableLiveData<String> = MutableLiveData()
    val authStatus: MutableLiveData<Boolean> = MutableLiveData()

    suspend fun insertAccount(account: AccountData){
        accountDao.insert(account)
    }

    // !!!!This is only used for PAT authentication, do not use it anywhere else!!!!
    /**
    * Returns true and empty string if auth succeeds
    * Returns false and exception string if auth fails
    */
    suspend fun authViaPat(): MutableLiveData<Boolean>{
        try {
            val networkCall = accountsService?.getPaginatedAccountType("asset", 1)
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                authStatus.postValue(true)
                responseApi.postValue("success")
            } else {
                authStatus.postValue(false)
                responseApi.postValue("There was an issue communicating with your server")
            }
        } catch (certificationException: CertificateException){
            responseApi.postValue("Are you using self signed cert?")
            authStatus.postValue(false)
        } catch (exception: Exception) {
            responseApi.postValue(exception.cause?.message)
            authStatus.postValue(false)
        }
        return authStatus
    }


    suspend fun getAccountByType(accountType: String): Flow<MutableList<AccountData>> {
        loadRemoteData(accountType)
        return accountDao.getAccountByType(accountType)
    }

    suspend fun retrieveAccountById(accountId: Long) = accountDao.getAccountById(accountId)

    suspend fun deleteAccountById(accountId: Long): Boolean {
        var isDeleted = false
        try {
            val networkResponse = accountsService?.deleteAccountById(accountId)
            if(networkResponse?.code() == 204){
                accountDao.deleteAccountById(accountId)
                isDeleted = true
            }
        } catch (exception: Exception){ }
        return isDeleted
    }

    suspend fun retrieveAccountByName(accountName: String) = accountDao.getAccountByName(accountName)

    private suspend fun deleteAccountByType(accountType: String): Int = accountDao.deleteAccountByType(accountType)

    private suspend fun loadRemoteData(accountType: String){
        val accountData: MutableList<AccountData> = arrayListOf()
        try {
            val networkCall = accountsService?.getPaginatedAccountType(accountType, 1)
            accountData.addAll(networkCall?.body()?.data?.toMutableList() ?: arrayListOf())
            val responseBody = networkCall?.body()
            apiResponse = networkCall?.message() ?: ""
            if (responseBody != null && networkCall.isSuccessful) {
                val pagination = responseBody.meta.pagination
                if (pagination.total_pages != pagination.current_page) {
                    for (items in 2..pagination.total_pages) {
                        accountData.addAll(
                                accountsService?.getPaginatedAccountType(accountType, items)?.body()?.data?.toMutableList()
                                        ?: arrayListOf()
                        )
                    }
                }
                deleteAccountByType(accountType)
                accountData.forEachIndexed { _, data ->
                    accountDao.insert(data)
                }
            }
        } catch (exception: Exception){ }
    }
}