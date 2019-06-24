package xyz.hisname.fireflyiii.repository.transaction

import androidx.work.Data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionSuccessModel
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.workers.transaction.DeleteTransactionWorker
import java.math.BigDecimal

@Suppress("RedundantSuspendModifier")
class TransactionRepository(private val transactionDao: TransactionDataDao,
                            private val transactionService: TransactionService?) {

    suspend fun insertTransaction(transaction: TransactionData){
        transactionDao.insert(transaction)
    }

    suspend fun allWithdrawalWithCurrencyCode(startDate: String, endDate: String, currencyCode: String): Double {
        loadRemoteData(startDate, endDate, "Withdrawal")
        return transactionDao.getTransactionsByTypeWithDateAndCurrencyCode(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), "Withdrawal", currencyCode)
    }


    suspend fun transactionList(startDate: String?, endDate: String?,source: String): MutableList<TransactionData>{
        loadRemoteData(startDate, endDate, source)
        return if(startDate.isNullOrBlank() || endDate.isNullOrBlank()){
            transactionDao.getTransactionList(source)
        } else {
            transactionDao.getTransactionList(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate),source)
        }
    }

    suspend fun allDepositWithCurrencyCode(startDate: String, endDate: String, currencyCode: String): Double {
        loadRemoteData(startDate, endDate, "Deposit")
        return transactionDao.getTransactionsByTypeWithDateAndCurrencyCode(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), "Deposit", currencyCode)
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
            transactionDao.getTransactionByDateAndNullBudgetAndCurrency(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
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
                                      transactionType: String): MutableList<String> {
        loadRemoteData(startDate, endDate, "all")
        return transactionDao.getUniqueBudgetByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, transactionType)
    }

    suspend fun recentTransactions(limit: Int): MutableList<TransactionData>{
        loadRemoteData("", "", "all")
        return transactionDao.getRecentTransactions(limit)
    }

    suspend fun getTransactionById(transactionId: Long): MutableList<TransactionData>{
        var networkCall: Response<TransactionModel>? = null
        val transactionData: MutableList<TransactionData> = arrayListOf()
        try {
            withContext(Dispatchers.IO) {
                withContext(Dispatchers.IO) {
                    networkCall = transactionService?.getTransactionById(transactionId)
                }
                transactionData.addAll(networkCall?.body()?.data?.toMutableList() ?: arrayListOf())
            }
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall?.isSuccessful != false) {
                withContext(Dispatchers.IO) {
                    transactionData.forEachIndexed { _, data ->
                        transactionDao.insert(data)
                    }
                }
            }
        } catch (exception: Exception){ }
        return transactionDao.getTransactionById(transactionId)
    }

    suspend fun deleteTransactionById(transactionId: Long, shouldUseWorker: Boolean = false): Boolean {
        var networkResponse: Response<TransactionSuccessModel>? = null
        withContext(Dispatchers.IO){
            networkResponse = transactionService?.deleteTransactionById(transactionId)
        }
        return if (networkResponse?.code() == 204 || networkResponse?.code() == 200){
            withContext(Dispatchers.IO) {
                transactionDao.deleteTransactionById(transactionId)
            }
            true
        } else {
            if(shouldUseWorker){
                DeleteTransactionWorker.setupWorker(Data.Builder(),transactionId)
            }
            false
        }
    }

    suspend fun getTransactionListByDateAndAccount(startDate: String, endDate: String,
                                                   accountName: String): MutableList<TransactionData>{
        loadRemoteData(startDate, endDate, "all")
        return transactionDao.getTransactionListByDateAndAccount(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), accountName)
    }
    suspend fun getTransactionListByDateAndBudget(startDate: String, endDate: String,
                                                  budgetName: String): MutableList<TransactionData> {
        loadRemoteData(startDate, endDate, "all")
        return transactionDao.getTransactionListByDateAndBudget(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), budgetName)
    }

    private suspend fun deleteTransactionsByDate(startDate: String?, endDate: String?, transactionType: String): Int{
        return if(startDate == null || endDate == null){
            transactionDao.deleteTransaction()
        } else {
            transactionDao.deleteTransactionsByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), transactionType)
        }
    }

    private suspend fun loadRemoteData(startDate: String?, endDate: String?, sourceName: String){
        var networkCall: Response<TransactionModel>? = null
        val transactionData: MutableList<TransactionData> = arrayListOf()
        try {
            withContext(Dispatchers.IO) {
                withContext(Dispatchers.IO) {
                    networkCall = transactionService?.getPaginatedTransactions(startDate, endDate,
                            convertString(sourceName), 1)
                }
                transactionData.addAll(networkCall?.body()?.data?.toMutableList() ?: arrayListOf())
            }
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall?.isSuccessful != false) {
                val pagination = responseBody.meta.pagination
                if (pagination.total_pages != pagination.current_page) {
                    withContext(Dispatchers.IO) {
                        for (items in 2..pagination.total_pages) {
                            transactionData.addAll(
                                    transactionService?.getPaginatedTransactions(startDate, endDate,
                                            convertString(sourceName), items)?.body()?.data?.toMutableList() ?: arrayListOf()
                            )
                        }
                    }
                }
                withContext(Dispatchers.IO) {
                    deleteTransactionsByDate(startDate, endDate, sourceName)
                }
                withContext(Dispatchers.IO) {
                    transactionData.forEachIndexed { _, data ->
                        transactionDao.insert(data)
                    }
                }
            }
        } catch (exception: Exception){ }
    }

    private fun convertString(type: String) = type.substring(0,1).toLowerCase() + type.substring(1).toLowerCase()

}