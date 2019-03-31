package xyz.hisname.fireflyiii.repository.transaction

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.util.DateTimeUtil

@Suppress("RedundantSuspendModifier")
class TransactionRepository(private val transactionDao: TransactionDataDao) {

    val allTransaction = transactionDao.getTransaction()

    @WorkerThread
    suspend fun insertTransaction(transaction: TransactionData){
        transactionDao.insert(transaction)
    }

    @WorkerThread
    suspend fun allWithdrawalWithCurrencyCode(startDate: String, endDate: String, currencyCode: String) =
            transactionDao.getTransactionsByTypeWithDateAndCurrencyCode(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), "Withdrawal", currencyCode)


    @WorkerThread
    fun transactionList(startDate: String?, endDate: String?,source: String): MutableList<TransactionData>{
        return if(startDate.isNullOrBlank() || endDate.isNullOrBlank()){
            transactionDao.getTransactionList(null, null,source)
        } else {
            transactionDao.getTransactionList(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate),source)
        }
    }

    @WorkerThread
    suspend fun allDepositWithCurrencyCode(startDate: String, endDate: String, currencyCode: String) =
            transactionDao.getTransactionsByTypeWithDateAndCurrencyCode(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), "Deposit", currencyCode)

    @WorkerThread
    suspend fun getTransactionsByAccountAndCurrencyCodeAndDate(startDate: String, endDate: String,
                                                                       currencyCode: String, accountName: String) =
            transactionDao.getTransactionsByAccountAndCurrencyCodeAndDate(
                    DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, accountName)


    @WorkerThread
    suspend fun getTotalTransactionType(startDate: String, endDate: String, currencyCode: String,
                                                  accountName: String, transactionType: String) =
            transactionDao.getTotalTransactionType(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, accountName, transactionType)



    @WorkerThread
    suspend fun getTransactionByDateAndCategoryAndCurrency(startDate: String, endDate: String, currencyCode: String,
                                                  accountName: String, transactionType: String, categoryName: String?): Double {
        return if(categoryName != null){
            transactionDao.getTransactionByDateAndCategoryAndCurrency(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, accountName, transactionType, categoryName)
        } else {
            transactionDao.getTransactionByDateAndNullCategoryAndCurrency(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, accountName, transactionType)
        }
    }

    @WorkerThread
    suspend fun getTransactionByDateAndBudgetAndCurrency(startDate: String, endDate: String, currencyCode: String,
                                                         accountName: String, transactionType: String,
                                                         budgetName: String?): Double {
        return if(budgetName != null){
            transactionDao.getTransactionByDateAndBudgetAndCurrency(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, accountName, transactionType, budgetName)
        } else {
            transactionDao.getTransactionByDateAndNullBudgetAndCurrency(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, accountName, transactionType)
        }
    }

    @WorkerThread
    suspend fun getUniqueCategoryByDate(startDate: String, endDate: String, currencyCode: String,
                                        sourceName: String, transactionType: String) =
            transactionDao.getUniqueCategoryByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, sourceName, transactionType)

    @WorkerThread
    suspend fun getUniqueBudgetByDate(startDate: String, endDate: String, currencyCode: String,
                                      sourceName: String, transactionType: String) =
            transactionDao.getUniqueBudgetByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, sourceName, transactionType)

    @WorkerThread
    suspend fun recentTransactions(limit: Int) = transactionDao.getRecentTransactions(limit)

    @WorkerThread
    suspend fun getTransactionById(transactionId: Long) = transactionDao.getTransactionById(transactionId)

    @WorkerThread
    suspend fun deleteTransactionById(transactionId: Long) = transactionDao.deleteTransactionById(transactionId)

    @WorkerThread
    suspend fun deleteTransactionsByDate(startDate: String?, endDate: String?, transactionType: String): Int{
        return transactionDao.deleteTransactionsByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate!!),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate!!), transactionType)
    }

    @WorkerThread
    suspend fun getTransactionListByDateAndAccount(startDate: String, endDate: String, accountName: String) =
            transactionDao.getTransactionListByDateAndAccount(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), accountName)

}