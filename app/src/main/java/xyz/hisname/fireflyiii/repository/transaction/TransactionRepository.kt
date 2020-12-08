package xyz.hisname.fireflyiii.repository.transaction

import android.net.Uri
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.transaction.*
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.debounce
import xyz.hisname.fireflyiii.util.network.HttpConstants
import java.math.BigDecimal

@Suppress("RedundantSuspendModifier")
class TransactionRepository(private val transactionDao: TransactionDataDao,
                            private val transactionService: TransactionService?) {

    suspend fun insertTransaction(transaction: Transactions){
        transactionDao.insert(transaction)
    }

    suspend fun insertTransaction(transactionIndex: TransactionIndex){
        transactionDao.insert(transactionIndex)
    }

    suspend fun transactionCount(startDate: String, endDate: String,source: String): Int {
        return transactionDao.getTransactionByDateCount(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), convertString(source))
    }

    suspend fun getTransactionByDateAndCurrencyCode(startDate: String, endDate: String,
                                                    currencyCode: String,
                                                    transactionType: String,
                                                    shouldLoadData: Boolean): BigDecimal{
        if(shouldLoadData){
            loadRemoteData(startDate, endDate, transactionType)
        }
        return transactionDao.getTransactionsByTypeWithDateAndCurrencyCode(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), transactionType, currencyCode)
    }

    suspend fun getTransactionsBySourceAndDate(startDate: String, endDate: String,
                                                               accountId: Long): BigDecimal{
        return transactionDao.getTransactionsBySourceAndDate(
                DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), accountId)
    }

    suspend fun getTransactionsByDestinationAndDate(startDate: String, endDate: String,
                                                      accountId: Long): BigDecimal{
        return transactionDao.getTransactionsByDestinationAndDate(
                DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), accountId)
    }


    suspend fun getTotalTransactionType(startDate: String, endDate: String,
                                        currencyCode: String, transactionType: String): BigDecimal{
        loadRemoteData(startDate, endDate, convertString(transactionType))
        return transactionDao.getTotalTransactionType(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, convertString(transactionType))
    }

    suspend fun getTransactionByDateAndBudgetAndCurrency(startDate: String, endDate: String,
                                                         currencyCode: String,
                                                         transactionType: String,
                                                         budgetName: String) =
        transactionDao.getTransactionByDateAndBudgetAndCurrency(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, transactionType, budgetName)


    suspend fun getUniqueBudgetByDate(startDate: String, endDate: String, currencyCode: String,
                                      transactionType: String)= transactionDao.getUniqueBudgetByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, transactionType)

    suspend fun getTransactionByJournalId(journalId: Long) = transactionDao.getTransactionByJournalId(journalId)

    suspend fun getTransactionIdFromJournalId(journalId: Long) =
            transactionDao.getTransactionIdFromJournalId(journalId)

    suspend fun deleteTransactionById(transactionId: Long): Int {
        try {
            val networkResponse = transactionService?.deleteTransactionById(transactionId)
            when(networkResponse?.code()) {
                204 -> {
                    transactionDao.deleteTransactionByJournalId(transactionId)
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
                    transactionDao.deleteTransactionByJournalId(transactionId)
                    return HttpConstants.NOT_FOUND
                }
                500 -> {
                    // When deleting transactions on some versions of Firefly, HTTP 500 is returned
                    transactionDao.deleteTransactionByJournalId(transactionId)
                    return HttpConstants.NO_CONTENT_SUCCESS
                }
                else -> {
                    return HttpConstants.FAILED
                }
            }
        } catch (exception: Exception) {
            transactionDao.deleteTransactionByJournalId(transactionId)
            return HttpConstants.FAILED
        }
    }

    suspend fun getUniqueCategoryBySourceAndDateAndType(accountId: Long,
                                                        startDate: String, endDate: String,
                                                        transactionType: String) =
            transactionDao.getUniqueCategoryBySourceAndDateAndType(accountId,
                    DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate),
                    transactionType)

    suspend fun getUniqueCategoryByDestinationAndDateAndType(accountId: Long,
                                                        startDate: String, endDate: String,
                                                        transactionType: String) =
            transactionDao.getUniqueCategoryByDestinationAndDateAndType(accountId,
                    DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate),
                    transactionType)


    suspend fun getUniqueBudgetBySourceAndDateAndType(accountId: Long,
                                                       startDate: String, endDate: String,
                                                       transactionType: String) =
            transactionDao.getUniqueBudgetBySourceAndDateAndType(accountId,
                    DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), transactionType)

    suspend fun getUniqueBudgetByDestinationAndDateAndType(accountId: Long,
                                                      startDate: String, endDate: String,
                                                      transactionType: String) =
            transactionDao.getUniqueBudgetByDestinationAndDateAndType(accountId,
                    DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), transactionType)

    suspend fun getTransactionByDescription(query: String): Flow<List<String>>{
        // Search via API only if query is more than 3
        if(query.length > 3){
            val handleSearch = debounce<String>(Dispatchers.IO){ debouncedString ->
                runBlocking {
                    try {
                        val networkCall = transactionService?.searchTransaction(debouncedString)
                        val responseBody = networkCall?.body()
                        if (responseBody != null && networkCall.isSuccessful) {
                            responseBody.data.forEach { data ->
                                data.transactionAttributes?.transactions?.forEach { transaction ->
                                    transactionDao.insert(transaction)
                                    transactionDao.insert(TransactionIndex(data.transactionId, transaction.transaction_journal_id))
                                }
                            }
                        }
                    } catch (exception: Exception){ }
                }
            }
            handleSearch(query)
        }
        return transactionDao.getTransactionByDescription("%$query%")
    }

    suspend fun addTransaction(type: String, description: String,
                               date: String, time: String?, piggyBankName: String?, amount: String,
                               sourceName: String?, destinationName: String?, currencyName: String,
                               category: String?, tags: String?, budgetName: String?,
                               notes: String?): ApiResponses<TransactionSuccessModel> {
        val dateTime = if (time == null) {
            date
        } else {
            DateTimeUtil.mergeDateTimeToIso8601(date, time)
        }
        return try {
            val networkCall = transactionService?.addTransaction(convertString(type), description, dateTime,
                    piggyBankName, amount.replace(',', '.'), sourceName,
                    destinationName, currencyName,
                    category, tags, budgetName, notes)
            parseResponse(networkCall)
        } catch (exception: Exception){
            ApiResponses(error = exception)
        }
    }

    suspend fun updateTransaction(transactionId: Long, type: String, description: String,
                                  date: String, time: String?, piggyBankName: String?, amount: String,
                                  sourceName: String?, destinationName: String?, currencyName: String,
                                  category: String?, tags: String?, budgetName: String?,
                                  notes: String): ApiResponses<TransactionSuccessModel> {
        val dateTime = if (time == null) {
            date
        } else {
            DateTimeUtil.mergeDateTimeToIso8601(date, time)
        }
        return try {
            val networkCall = transactionService?.updateTransaction(getTransactionIdFromJournalId(transactionId),
                    convertString(type),description, dateTime, piggyBankName,
                    amount.replace(',', '.'),sourceName,destinationName,currencyName,
                    category, tags, budgetName, notes)
            parseResponse(networkCall)
        } catch (exception: Exception){
            ApiResponses(error = exception)
        }
    }


    private suspend fun parseResponse(responseFromServer: Response<TransactionSuccessModel>?): ApiResponses<TransactionSuccessModel>{
        val responseBody = responseFromServer?.body()
        val responseErrorBody = responseFromServer?.errorBody()
        if(responseBody != null && responseFromServer.isSuccessful){
            if(responseErrorBody != null){
                // Ignore lint warning. False positive
                // https://github.com/square/retrofit/issues/3255#issuecomment-557734546
                var errorMessage = String(responseErrorBody.bytes())
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorMessage)
                errorMessage = when {
                    moshi?.errors?.transactions_currency != null -> moshi.errors.transactions_currency[0]
                    moshi?.errors?.piggy_bank_name != null -> moshi.errors.piggy_bank_name[0]
                    moshi?.errors?.transactions_destination_name != null -> moshi.errors.transactions_destination_name[0]
                    moshi?.errors?.transactions_source_name  != null -> moshi.errors.transactions_source_name[0]
                    moshi?.errors?.transaction_destination_id  != null -> moshi.errors.transaction_destination_id[0]
                    moshi?.errors?.transaction_amount != null -> moshi.errors.transaction_amount[0]
                    moshi?.errors?.description != null -> moshi.errors.description[0]
                    else -> "The given data was invalid"
                }
                return ApiResponses(errorMessage = errorMessage)
            } else {
                responseBody.data.transactionAttributes?.transactions?.forEach { transaction ->
                    insertTransaction(transaction)
                    insertTransaction(TransactionIndex(responseBody.data.transactionId,
                            transaction.transaction_journal_id))
                }
                return ApiResponses(response = responseBody)
            }
        } else {
            return ApiResponses(errorMessage = "Error occurred while saving transactions")
        }
    }

    private suspend fun loadRemoteData(startDate: String?, endDate: String?, sourceName: String){
        try {
            val transactionData: MutableList<TransactionData> = arrayListOf()
            val networkCall = transactionService?.getPaginatedTransactions(startDate, endDate,
                    convertString(sourceName), 1)
            networkCall?.body()?.data?.toMutableList()?.forEach { transaction ->
                transactionData.add(transaction)
            }
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                val pagination = responseBody.meta.pagination
                if (pagination.total_pages != pagination.current_page) {
                    for (items in 2..pagination.total_pages) {
                        val service = transactionService?.getPaginatedTransactions(startDate, endDate,
                                convertString(sourceName), items)?.body()
                        service?.data?.forEach { dataToBeAdded ->
                            transactionData.add(dataToBeAdded)
                        }
                    }
                }
                deleteTransactionsByDate(startDate, endDate, sourceName)
                transactionData.forEach { data ->
                    transactionDao.insert(data.transactionAttributes?.transactions!![0])
                    transactionDao.insert(TransactionIndex(data.transactionId,
                            data.transactionAttributes?.transactions?.get(0)?.transaction_journal_id))
                }
            }
        } catch(exception: Exception){ }
    }

    private suspend fun deleteTransactionsByDate(startDate: String?, endDate: String?, transactionType: String): Int{
        return if(startDate == null || endDate == null || startDate.isBlank() || endDate.isBlank()){
            transactionDao.deleteTransaction()
        } else {
            transactionDao.deleteTransactionsByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), transactionType)
        }
    }

    private fun convertString(type: String) = type.substring(0,1).toLowerCase() + type.substring(1).toLowerCase()

}