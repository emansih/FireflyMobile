package xyz.hisname.fireflyiii.repository.transaction

import android.content.Context
import androidx.work.Data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.transaction.*
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.workers.transaction.DeleteTransactionWorker
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

    suspend fun allWithdrawalWithCurrencyCode(startDate: String, endDate: String, currencyCode: String): Double {
        loadRemoteData(startDate, endDate, "Withdrawal")
        return transactionDao.getTransactionsByTypeWithDateAndCurrencyCode(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), "withdrawal", currencyCode)
    }


    suspend fun transactionList(startDate: String?, endDate: String?,source: String, pageNumber: Int): MutableList<Transactions>{
        return if(startDate == null || endDate == null){
            loadPaginatedData("", "", source, pageNumber)
            transactionDao.getTransactionLimitByType(convertString(source), pageNumber * Constants.PAGE_SIZE)
        } else {
            loadPaginatedData(startDate, endDate, source, pageNumber)
            transactionDao.getTransactionLimitByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate),convertString(source), Constants.PAGE_SIZE)
        }
    }

    suspend fun transactionList(startDate: String?, endDate: String?,source: String): MutableList<Transactions> {
        loadRemoteData(startDate, endDate, source)
        return if (startDate.isNullOrBlank() || endDate.isNullOrBlank()) {
            transactionDao.getTransactionList(convertString(source))
        } else {
            transactionDao.getTransactionList(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), convertString(source))
        }
    }

    suspend fun allDepositWithCurrencyCode(startDate: String, endDate: String, currencyCode: String): Double {
        loadRemoteData(startDate, endDate, "Deposit")
        return transactionDao.getTransactionsByTypeWithDateAndCurrencyCode(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), "deposit", currencyCode)
    }

    suspend fun getTransactionsByAccountAndCurrencyCodeAndDate(startDate: String, endDate: String,
                                                                       currencyCode: String,
                                                               accountName: String): BigDecimal{
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

    suspend fun getTotalTransactionType(startDate: String, endDate: String, currencyCode: String, transactionType: String): Double{
        loadRemoteData(startDate, endDate, "all")
        return transactionDao.getTotalTransactionType(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, transactionType)
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

    suspend fun getTransactionByDateAndBudgetAndCurrency(startDate: String, endDate: String, currencyCode: String,
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

    suspend fun getTransactionByDateAndBudgetAndCurrency(startDate: String, endDate: String, currencyCode: String,
                                                         transactionType: String, budgetName: String?): Double {
        loadRemoteData(startDate, endDate, "all")
        return if(budgetName != null){
            transactionDao.getTransactionByDateAndBudgetAndCurrency(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, transactionType, budgetName)
        } else {
            transactionDao.getTransactionAmountByDateAndNullBudgetAndCurrency(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, transactionType)
        }
    }

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
                                      transactionType: String): MutableList<String?> {
        loadRemoteData(startDate, endDate, "all")
        return transactionDao.getUniqueBudgetByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, transactionType)
    }

    suspend fun recentTransactions(limit: Int): MutableList<Transactions>{
        loadRemoteData("", "", "all")
        val listOfTransactions = arrayListOf<Transactions>()
        transactionDao.getTransactionLimit(limit).forEachIndexed { _, transactionIndex ->
            listOfTransactions.addAll(transactionDao.getTransactionFromJournalId(transactionIndex.transactionJournalId ?: 0L))
        }
        return listOfTransactions
    }

    suspend fun getTransactionByJournalId(journalId: Long) = transactionDao.getTransactionByJournalId(journalId)

    suspend fun getTransactionIdFromJournalId(journalId: Long) =
            transactionDao.getTransactionIdFromJournalId(journalId)

    suspend fun getTransactionById(transactionId: Long): MutableList<Transactions>{
        var networkCall: Response<TransactionModel>? = null
        val transactionData: MutableList<TransactionData> = arrayListOf()
        try {
            withContext(Dispatchers.IO) {
                withContext(Dispatchers.IO) {
                    networkCall = transactionService?.getTransactionById(transactionId)
                }
                transactionData.addAll(networkCall?.body()?.data?.toMutableList() ?: arrayListOf())
            }
            networkCall?.body()?.data?.forEachIndexed { _, transaction ->
                transactionDao.insert(TransactionIndex(transaction.transactionId,
                        transaction.transactionAttributes?.transactions?.get(0)?.transaction_journal_id))
            }
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall?.isSuccessful != false) {
                withContext(Dispatchers.IO) {
                    transactionData.forEachIndexed { _, data ->
                        transactionDao.insert(data.transactionAttributes?.transactions!![0])
                        transactionDao.insert(TransactionIndex(data.transactionId,
                                data.transactionAttributes?.transactions?.get(0)?.transaction_journal_id))
                    }
                }
            }
        } catch (exception: Exception){ }
        var transactionJournalId = 0L
        withContext(Dispatchers.IO){
            transactionJournalId = transactionDao.getTransactionIdFromJournalId(transactionId)
        }
        return transactionDao.getTransactionFromJournalId(transactionJournalId)
    }

    suspend fun deleteTransactionById(transactionId: Long, shouldUseWorker: Boolean = false, context: Context): Boolean {
        var networkResponse: Response<TransactionSuccessModel>? = null
        withContext(Dispatchers.IO){
            networkResponse = transactionService?.deleteTransactionById(transactionId)
        }
        return if (networkResponse?.code() == 204 || networkResponse?.code() == 200){
            withContext(Dispatchers.IO) {
                val journalId = transactionDao.getTransactionIdFromJournalId(transactionId)
                transactionDao.deleteTransactionByJournalId(journalId)
            }
            true
        } else {
            if(shouldUseWorker){
                DeleteTransactionWorker.setupWorker(Data.Builder(),transactionId, context)
            }
            false
        }
    }

    suspend fun getTransactionListByDateAndAccount(startDate: String, endDate: String,
                                                   accountName: String): MutableList<Transactions>{
        loadRemoteData(startDate, endDate, "all")
        return transactionDao.getTransactionListByDateAndAccount(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), accountName)
    }
    suspend fun getTransactionListByDateAndBudget(startDate: String, endDate: String,
                                                  budgetName: String?): MutableList<Transactions> {
        loadRemoteData(startDate, endDate, "all")
        return if(budgetName != null){
            transactionDao.getTransactionListByDateAndBudget(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), budgetName)
        } else {
            transactionDao.getTransactionByDateAndNullBudgetAndCurrency(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate))
        }
    }

    private suspend fun deleteTransactionsByDate(startDate: String?, endDate: String?, transactionType: String): Int{
        return if(startDate == null || endDate == null || startDate.isBlank() || endDate.isBlank()){
            transactionDao.deleteTransaction()
        } else {
            transactionDao.deleteTransactionsByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), transactionType)
        }
    }

    private suspend fun loadPaginatedData(startDate: String, endDate: String, sourceName: String, pageNumber: Int){
        val  networkCall = transactionService?.getPaginatedTransactions(startDate, endDate,
                convertString(sourceName), pageNumber)
        val responseBody = networkCall?.body()
        if (responseBody != null && networkCall.isSuccessful) {
            if(pageNumber == 1){
                transactionDao.deleteTransaction()
            }
            responseBody.data.forEachIndexed { _, data ->
                transactionDao.insert(data.transactionAttributes?.transactions!![0])
                transactionDao.insert(TransactionIndex(data.transactionId,
                        data.transactionAttributes?.transactions?.get(0)?.transaction_journal_id))

            }
        }
    }

    private suspend fun loadRemoteData(startDate: String?, endDate: String?, sourceName: String){
        val transactionData: MutableList<TransactionData> = arrayListOf()
        val networkCall = transactionService?.getPaginatedTransactions(startDate, endDate,
                    convertString(sourceName), 1)
        networkCall?.body()?.data?.toMutableList()?.forEachIndexed { _, transaction ->
            transactionData.add(transaction)
        }
        val responseBody = networkCall?.body()
        if (responseBody != null && networkCall.isSuccessful) {
            val pagination = responseBody.meta.pagination
            if (pagination.total_pages != pagination.current_page) {
                for (items in 2..pagination.total_pages) {
                    val service = transactionService?.getPaginatedTransactions(startDate, endDate,
                            convertString(sourceName), items)?.body()
                    service?.data?.forEachIndexed { _, dataToBeAdded ->
                        transactionData.add(dataToBeAdded)
                    }
                }
            }

            deleteTransactionsByDate(startDate, endDate, sourceName)
            transactionData.forEachIndexed { _, data ->
                transactionDao.insert(data.transactionAttributes?.transactions!![0])
                transactionDao.insert(TransactionIndex(data.transactionId,
                        data.transactionAttributes?.transactions?.get(0)?.transaction_journal_id))
            }
        }
    }

    private fun convertString(type: String) = type.substring(0,1).toLowerCase() + type.substring(1).toLowerCase()

}