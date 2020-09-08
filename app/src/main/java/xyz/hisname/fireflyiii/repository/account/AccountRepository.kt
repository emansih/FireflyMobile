package xyz.hisname.fireflyiii.repository.account

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.runBlocking
import xyz.hisname.fireflyiii.data.local.dao.AccountsDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.util.extension.debounce
import xyz.hisname.fireflyiii.util.network.HttpConstants

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
    @Throws(Exception::class)
    suspend fun authViaPat(): MutableLiveData<Boolean>{
        val networkCall = accountsService?.getPaginatedAccountType("asset", 1)
        val responseBody = networkCall?.body()
        if (responseBody != null && networkCall.isSuccessful) {
            authStatus.postValue(true)
            responseApi.postValue("success")
        }
        return authStatus
    }


    suspend fun getAccountByType(accountType: String): Flow<MutableList<AccountData>> {
        loadRemoteData(accountType)
        return accountDao.getAccountByType(accountType).distinctUntilChanged()
    }

    suspend fun retrieveAccountById(accountId: Long) = accountDao.getAccountById(accountId)

    suspend fun deleteAccountById(accountId: Long): Int {
        try {
            val networkResponse = accountsService?.deleteAccountById(accountId)
            when (networkResponse?.code()) {
                204 -> {
                    accountDao.deleteAccountById(accountId)
                    return HttpConstants.NO_CONTENT_SUCCESS
                }
                401 -> {
                    /*   User is unauthenticated. We will retain user's data as we are
                     *   now in inconsistent state. This use case is unlikely to happen unless user
                     *   deletes their token from the web interface without updating the mobile client
                     */
                    return HttpConstants.UNAUTHORISED
                }
                404 -> {
                    // User probably deleted this on the web interface and tried to do it using mobile client
                    accountDao.deleteAccountById(accountId)
                    return HttpConstants.NOT_FOUND
                }
                else -> {
                    return HttpConstants.FAILED
                }
            }
        } catch (exception: Exception){
            accountDao.deleteAccountById(accountId)
            return HttpConstants.FAILED
        }
    }

    suspend fun retrieveAccountByName(accountName: String) = accountDao.getAccountByName(accountName)

    suspend fun getAccountByNameAndType(accountType: String, accountName: String): Flow<MutableList<AccountData>>{
        if(accountName.length > 3){
            val handleSearch = debounce<String>(Dispatchers.IO){ debouncedString ->
                runBlocking {
                    try {
                        val networkCall = accountsService?.searchAccount(debouncedString, accountType)
                        val responseBody = networkCall?.body()
                        if (responseBody != null && networkCall.isSuccessful) {
                            responseBody.data.forEach { data ->
                                insertAccount(data)
                            }
                        }
                    } catch (exception: Exception){ }
                }
            }
            handleSearch(accountName)
        }
        return accountDao.getAccountByNameAndType(accountType, "%$accountName%")
    }

    private suspend fun deleteAccountByType(accountType: String): Int = accountDao.deleteAccountByType(accountType)

    @Deprecated("This is a very expensive network call. Use getAccountByNameAndType() instead")
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