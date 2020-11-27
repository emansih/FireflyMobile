package xyz.hisname.fireflyiii.repository.transaction

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
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

    suspend fun getTransactionsByAccountAndCurrencyCodeAndDate(startDate: String, endDate: String,
                                                                       currencyCode: String,
                                                               accountName: String): Double{
        loadRemoteData(startDate, endDate, "all")
        return transactionDao.getTransactionsByAccountAndCurrencyCodeAndDate(
                DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, accountName)
    }


    suspend fun getTotalTransactionType(startDate: String, endDate: String, currencyCode: String,
                                                  accountName: String, transactionType: String): Double {
        loadRemoteData(startDate, endDate, "all")
        return transactionDao.getTotalTransactionType(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, accountName, transactionType)
    }

    suspend fun getTotalTransactionType(startDate: String, endDate: String,
                                        currencyCode: String, transactionType: String): BigDecimal{
        loadRemoteData(startDate, endDate, convertString(transactionType))
        return transactionDao.getTotalTransactionType(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, convertString(transactionType))
    }

    suspend fun getTransactionByDateAndCategoryAndCurrency(startDate: String, endDate: String, currencyCode: String,
                                                  accountName: String, transactionType: String, categoryName: String?): Double {
        loadRemoteData(startDate, endDate, "all")
        return if(categoryName != null){
            transactionDao.getTransactionByDateAndCategoryAndCurrency(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, accountName, transactionType, categoryName)
        } else {
            transactionDao.getTransactionByDateAndNullCategoryAndCurrency(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, accountName, transactionType)
        }
    }

    suspend fun getTransactionByDateAndBudgetAndCurrencyAndAccountName(startDate: String, endDate: String, currencyCode: String,
                                                                       accountName: String, transactionType: String,
                                                                       budgetName: String?): Double {
        loadRemoteData(startDate, endDate, "all")
        return if(budgetName != null){
            transactionDao.getTransactionByDateAndBudgetAndCurrency(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, accountName, transactionType, budgetName)
        } else {
            transactionDao.getTransactionByDateAndNullBudgetAndCurrency(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, accountName, transactionType)
        }
    }

    suspend fun getTransactionByDateAndBudgetAndCurrency(startDate: String, endDate: String,
                                                         currencyCode: String,
                                                         transactionType: String,
                                                         budgetName: String) =
        transactionDao.getTransactionByDateAndBudgetAndCurrency(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, transactionType, budgetName)



    suspend fun getUniqueCategoryByDate(startDate: String, endDate: String, currencyCode: String,
                                        sourceName: String, transactionType: String): MutableList<String>{
        loadRemoteData(startDate, endDate, "all")
        return transactionDao.getUniqueCategoryByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, sourceName, transactionType)
    }

    suspend fun getUniqueBudgetByDate(startDate: String, endDate: String, currencyCode: String,
                                      sourceName: String, transactionType: String): MutableList<String> {
        loadRemoteData(startDate, endDate, "all")
        return transactionDao.getUniqueBudgetByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, sourceName, transactionType)
    }

    suspend fun getUniqueBudgetByDate(startDate: String, endDate: String, currencyCode: String,
                                      transactionType: String)= transactionDao.getUniqueBudgetByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, transactionType)

    suspend fun getTransactionByJournalId(journalId: Long) = transactionDao.getTransactionByJournalId(journalId)

    suspend fun getTransactionIdFromJournalId(journalId: Long) =
            transactionDao.getTransactionIdFromJournalId(journalId)

    suspend fun deleteTransactionById(transactionId: Long): Int {
        try {
            val networkResponse: Response<TransactionSuccessModel>? = transactionService?.deleteTransactionById(transactionId)
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
                else -> {
                    return HttpConstants.FAILED
                }
            }
        } catch (exception: Exception) {
            transactionDao.deleteTransactionByJournalId(transactionId)
            return HttpConstants.FAILED
        }
    }

    suspend fun getTransactionListByDateAndAccount(startDate: String, endDate: String,
                                                   accountName: String): MutableList<Transactions>{
        loadRemoteData(startDate, endDate, "all")
        return transactionDao.getTransactionListByDateAndAccount(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), accountName)
    }

    private suspend fun deleteTransactionsByDate(startDate: String?, endDate: String?, transactionType: String): Int{
        return if(startDate == null || endDate == null || startDate.isBlank() || endDate.isBlank()){
            transactionDao.deleteTransaction()
        } else {
            transactionDao.deleteTransactionsByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), transactionType)
        }
    }

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

    private fun convertString(type: String) = type.substring(0,1).toLowerCase() + type.substring(1).toLowerCase()

}