package xyz.hisname.fireflyiii.repository.account

import android.content.Context
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.AccountsDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.models.accounts.AccountsModel
import xyz.hisname.fireflyiii.workers.account.DeleteAccountWorker
import java.security.cert.CertPathValidatorException

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
        var networkCall: Response<AccountsModel>? = null
        try {
            withContext(Dispatchers.IO) {
                networkCall = accountsService?.getPaginatedAccountType("asset", 1)
            }
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall?.isSuccessful != false) {
                authStatus.postValue(true)
            } else {
                authStatus.postValue(false)
                responseApi.setValue("There was an issue communicating with your server")
            }
        } catch (exception: Exception) {
            if (exception.cause is CertPathValidatorException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (CertPathValidatorException().reason == CertPathValidatorException.BasicReason.EXPIRED) {
                        responseApi.postValue("Your SSL certificate has expired")
                    } else if (CertPathValidatorException().reason == CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED) {
                        responseApi.postValue("The public key or the signature algorithm has been constrained")
                    } else if (CertPathValidatorException().reason == CertPathValidatorException.BasicReason.INVALID_SIGNATURE) {
                        responseApi.postValue("Your SSL certificate has invalid signature")
                    } else if (CertPathValidatorException().reason == CertPathValidatorException.BasicReason.NOT_YET_VALID) {
                        responseApi.postValue("Your SSL certificate is not yet valid")
                    } else if (CertPathValidatorException().reason == CertPathValidatorException.BasicReason.REVOKED) {
                        responseApi.postValue("Your SSL certificate has been revoked")
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            responseApi.postValue("Are you using a self signed cert? Android P doesn't support it out of the box")
                        } else {
                            responseApi.postValue(exception.localizedMessage)
                        }
                    }
                } else {
                    responseApi.postValue(exception.localizedMessage)
                }
            } else {
                responseApi.postValue(exception.localizedMessage)
            }
            authStatus.postValue(false)
        }
        return authStatus
    }


    suspend fun getAccountByType(accountType: String): MutableList<AccountData>{
        loadRemoteData(accountType)
        return accountDao.getAccountByType(accountType)
    }

    suspend fun retrieveAccountById(accountId: Long) = accountDao.getAccountById(accountId)

    suspend fun deleteAccountById(accountId: Long, shouldUseWorker: Boolean = false, context: Context): Boolean {
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
                DeleteAccountWorker.deleteWorker(accountId, context)
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