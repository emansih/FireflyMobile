package xyz.hisname.fireflyiii.repository.account

import androidx.lifecycle.MutableLiveData
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.AccountsDataDao
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.models.accounts.AccountSuccessModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.util.extension.debounce
import xyz.hisname.fireflyiii.util.network.HttpConstants

@Suppress("RedundantSuspendModifier")
class AccountRepository(private val accountDao: AccountsDataDao,
                        private val accountsService: AccountsService?){

    private lateinit var apiResponse: String
    val authStatus: MutableLiveData<Boolean> = MutableLiveData()

    // !!!!This is only used for PAT authentication, do not use it anywhere else!!!!
    /**
    * Returns true if auth succeeds
    * Returns false and throws exception if auth fails
    */
    @Throws(Exception::class)
    suspend fun authViaPat(): Boolean{
        val networkCall = accountsService?.getPaginatedAccountType("asset", 1)
        val responseBody = networkCall?.body()
        return if (responseBody != null && networkCall.isSuccessful) {
            authStatus.postValue(true)
            true
        } else {
            false
        }
    }


    suspend fun getAccountByType(accountType: String): List<AccountData> {
        loadRemoteData(accountType)
        return accountDao.getAccountsByType(accountType)
    }

    suspend fun getAccountById(accountId: Long): AccountData{
        val accountName = accountDao.getAccountById(accountId).accountAttributes?.name
        if(accountName.isNullOrEmpty()){
            try {
                val networkCall = accountsService?.getAccountById(accountId)
                val responseBody = networkCall?.body()
                if(responseBody != null && networkCall.isSuccessful) {
                    responseBody.data.forEach { accountData ->
                        accountDao.insert(accountData)
                    }
                }
            } catch (exception: Exception){ }
        }
        return accountDao.getAccountById(accountId)
    }

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

    suspend fun getAccountByName(accountName: String, accountType: String): AccountData{
        val accountData = accountDao.getAccountByNameAndType(accountName, accountType)
        if(accountData.accountAttributes?.name.isNullOrEmpty()) {
            try {
                val networkCall = accountsService?.searchAccount(accountName, accountType)
                val responseBody = networkCall?.body()
                if (responseBody != null && networkCall.isSuccessful) {
                    responseBody.data.forEach { data ->
                        accountDao.insert(data)
                    }
                }
            } catch (exception: Exception) {
            }
        }
        return accountDao.getAccountByNameAndType(accountName, accountType)
    }

    suspend fun getTransactionByAccountId(accountId: Long, startDate: String,
                                          endDate: String, type: String,
                                          transactionDao: TransactionDataDao){
        try {
            val networkCall = accountsService?.getTransactionsByAccountId(accountId, 1, startDate, endDate, type)
            val responseBody = networkCall?.body()
            if(responseBody != null && networkCall.isSuccessful){
                responseBody.data.forEach { transactionData ->
                    transactionData.transactionAttributes?.transactions?.forEach { transactions ->
                        transactionDao.insert(transactions)
                        transactionDao.insert(TransactionIndex(
                                transactionData.transactionId,
                                transactions.transaction_journal_id
                        ))
                    }
                }
                val pagination = responseBody.meta.pagination
                if (pagination.total_pages != pagination.current_page) {
                    for (items in 2..pagination.total_pages) {
                        accountsService?.getTransactionsByAccountId(accountId, items, startDate, endDate, "all")
                    }
                }
            }
        } catch (exception: Exception){ }
    }

    suspend fun getAccountByNameAndType(accountType: String, accountName: String): Flow<List<String>>{
        if(accountName.length > 3){
            val handleSearch = debounce<String>(Dispatchers.IO){ debouncedString ->
                runBlocking {
                    try {
                        val networkCall = accountsService?.searchAccount(debouncedString, accountType)
                        val responseBody = networkCall?.body()
                        if (responseBody != null && networkCall.isSuccessful) {
                            responseBody.data.forEach { data ->
                                accountDao.insert(data)
                            }
                        }
                    } catch (exception: Exception){ }
                }
            }
            handleSearch(accountName)
        }
        return accountDao.searchAccountByNameAndType(accountType, "%$accountName%")
    }

    suspend fun addAccount(accountName: String, accountType: String,
                           currencyCode: String?, iban: String?, bic: String?, accountNumber: String?,
                           openingBalance: String?, openingBalanceDate: String?, accountRole: String?,
                           virtualBalance: String?, includeInNetWorth: Boolean, notes: String?, liabilityType: String?,
                           liabilityAmount: String?, liabilityStartDate: String?, interest: String?, interestPeriod: String?): ApiResponses<AccountSuccessModel>{
        return try {
            val networkCall = accountsService?.addAccount(accountName, accountType, currencyCode, iban, bic, accountNumber,
                    openingBalance, openingBalanceDate, accountRole, virtualBalance, includeInNetWorth,
                    notes, liabilityType, liabilityAmount, liabilityStartDate, interest, interestPeriod)
            parseResponse(networkCall)
        } catch (exception: Exception){
            ApiResponses(error = exception)
        }
    }

    suspend fun updateAccount(accountId: Long, accountName: String, accountType: String,
                      currencyCode: String?, iban: String?, bic: String?, accountNumber: String?,
                      openingBalance: String?, openingBalanceDate: String?, accountRole: String?,
                      virtualBalance: String?, includeInNetWorth: Boolean, notes: String?, liabilityType: String?,
                      liabilityAmount: String?, liabilityStartDate: String?, interest: String?, interestPeriod: String?): ApiResponses<AccountSuccessModel>{
        return try {
            val networkCall = accountsService?.updateAccount(accountId, accountName, accountType, currencyCode, iban, bic, accountNumber,
                    openingBalance, openingBalanceDate, accountRole, virtualBalance, includeInNetWorth,
                    notes, liabilityType, liabilityAmount, liabilityStartDate, interest, interestPeriod)
            parseResponse(networkCall)
        } catch (exception: Exception){
            ApiResponses(error = exception)
        }
    }


    private suspend fun parseResponse(responseFromServer: Response<AccountSuccessModel>?): ApiResponses<AccountSuccessModel>{
        val responseBody = responseFromServer?.body()
        val responseErrorBody = responseFromServer?.errorBody()
        if(responseBody != null && responseFromServer.isSuccessful){
            if(responseErrorBody != null){
                // Ignore lint warning. False positive
                // https://github.com/square/retrofit/issues/3255#issuecomment-557734546
                var errorMessage = String(responseErrorBody.bytes())
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorMessage)
                errorMessage = when {
                    moshi?.errors?.name != null -> moshi.errors.name[0]
                    moshi?.errors?.account_number != null -> moshi.errors.account_number[0]
                    moshi?.errors?.interest != null -> moshi.errors.interest[0]
                    moshi?.errors?.liabilityStartDate != null -> moshi.errors.liabilityStartDate[0]
                    moshi?.errors?.currency_code != null -> moshi.errors.currency_code[0]
                    moshi?.errors?.iban != null -> moshi.errors.iban[0]
                    moshi?.errors?.bic != null -> moshi.errors.bic[0]
                    moshi?.errors?.opening_balance != null -> moshi.errors.opening_balance[0]
                    moshi?.errors?.opening_balance_date != null -> moshi.errors.opening_balance_date[0]
                    moshi?.errors?.interest_period != null -> moshi.errors.interest_period[0]
                    moshi?.errors?.liability_amount != null -> moshi.errors.liability_amount[0]
                    moshi?.errors?.exception != null -> moshi.errors.exception[0]
                    else -> "Error occurred while saving Account"
                }
                return ApiResponses(errorMessage = errorMessage)
            } else {
                accountDao.insert(responseBody.data)
                return ApiResponses(response = responseBody)
            }
        } else {
            return ApiResponses(errorMessage = "Error occurred while saving Account")
        }
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