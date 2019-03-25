package xyz.hisname.fireflyiii.repository.transaction

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.util.DateTimeUtil

class TransactionRepository(private val transactionDao: TransactionDataDao) {

    val allTransaction = transactionDao.getTransaction()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertTransaction(transaction: TransactionData){
        transactionDao.insert(transaction)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun allWithdrawalWithCurrencyCode(startDate: String, endDate: String, currencyCode: String) =
            transactionDao.getTransactionsByTypeWithDateAndCurrencyCode(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), "Withdrawal", currencyCode)

    fun transactionList(startDate: String?, endDate: String?,source: String): MutableList<TransactionData>{
        return if(startDate.isNullOrBlank() || endDate.isNullOrBlank()){
            transactionDao.getTransactionList(null, null,source)
        } else {
            transactionDao.getTransactionList(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate),source)
        }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun allDepositWithCurrencyCode(startDate: String, endDate: String, currencyCode: String) =
            transactionDao.getTransactionsByTypeWithDateAndCurrencyCode(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), "Deposit", currencyCode)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getTransactionsByAccountAndCurrencyCodeAndDate(startDate: String, endDate: String,
                                                                       currencyCode: String, accountName: String) =
            transactionDao.getTransactionsByAccountAndCurrencyCodeAndDate(
                    DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, accountName)


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getTotalTransactionTypeByCategory(startDate: String, endDate: String, currencyCode: String,
                                                  accountName: String, transactionType: String) =
            transactionDao.getTotalTransactionTypeByCategory(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, accountName, transactionType)



    @Suppress("RedundantSuspendModifier")
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

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getUniqueCategoryByDate(startDate: String, endDate: String, currencyCode: String,
                                        sourceName: String, transactionType: String) =
            transactionDao.getUniqueCategoryByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, sourceName, transactionType)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun recentTransactions(limit: Int) = transactionDao.getRecentTransactions(limit)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getTransactionById(transactionId: Long) = transactionDao.getTransactionById(transactionId)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteTransactionById(transactionId: Long) = transactionDao.deleteTransactionById(transactionId)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteTransactionsByDate(startDate: String?, endDate: String?, transactionType: String): Int{
        return transactionDao.deleteTransactionsByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate!!),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate!!), transactionType)
    }

}