package xyz.hisname.fireflyiii.repository.transaction

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.util.DateTimeUtil

@Suppress("RedundantSuspendModifier")
@WorkerThread
class TransactionRepository(private val transactionDao: TransactionDataDao) {

    val allTransaction = transactionDao.getTransaction()

    suspend fun insertTransaction(transaction: TransactionData){
        transactionDao.insert(transaction)
    }

    suspend fun allWithdrawalWithCurrencyCode(startDate: String, endDate: String, currencyCode: String) =
            transactionDao.getTransactionsByTypeWithDateAndCurrencyCode(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), "Withdrawal", currencyCode)


    suspend fun transactionList(startDate: String?, endDate: String?,source: String): MutableList<TransactionData>{
        return if(startDate.isNullOrBlank() || endDate.isNullOrBlank()){
            transactionDao.getTransactionList(source)
        } else {
            transactionDao.getTransactionList(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate),source)
        }
    }

    suspend fun allDepositWithCurrencyCode(startDate: String, endDate: String, currencyCode: String) =
            transactionDao.getTransactionsByTypeWithDateAndCurrencyCode(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), "Deposit", currencyCode)

    suspend fun getTransactionsByAccountAndCurrencyCodeAndDate(startDate: String, endDate: String,
                                                                       currencyCode: String, accountName: String) =
            transactionDao.getTransactionsByAccountAndCurrencyCodeAndDate(
                    DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, accountName)


    suspend fun getTotalTransactionType(startDate: String, endDate: String, currencyCode: String,
                                                  accountName: String, transactionType: String) =
            transactionDao.getTotalTransactionType(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, accountName, transactionType)

    suspend fun getTotalTransactionType(startDate: String, endDate: String, currencyCode: String, transactionType: String) =
            transactionDao.getTotalTransactionType(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, transactionType)

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

    suspend fun getTransactionByDateAndBudgetAndCurrency(startDate: String, endDate: String, currencyCode: String,
                                                         transactionType: String, budgetName: String?): Double {
        return if(budgetName != null){
            transactionDao.getTransactionByDateAndBudgetAndCurrency(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, transactionType, budgetName)
        } else {
            transactionDao.getTransactionByDateAndNullBudgetAndCurrency(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, transactionType)
        }
    }

    suspend fun getUniqueCategoryByDate(startDate: String, endDate: String, currencyCode: String,
                                        sourceName: String, transactionType: String) =
            transactionDao.getUniqueCategoryByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, sourceName, transactionType)

    suspend fun getUniqueBudgetByDate(startDate: String, endDate: String, currencyCode: String,
                                      sourceName: String, transactionType: String) =
            transactionDao.getUniqueBudgetByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, sourceName, transactionType)

    suspend fun getUniqueBudgetByDate(startDate: String, endDate: String, currencyCode: String,
                                      transactionType: String) =
            transactionDao.getUniqueBudgetByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), currencyCode, transactionType)

    suspend fun recentTransactions(limit: Int) = transactionDao.getRecentTransactions(limit)

    suspend fun getTransactionById(transactionId: Long) = transactionDao.getTransactionById(transactionId)

    suspend fun deleteTransactionById(transactionId: Long) = transactionDao.deleteTransactionById(transactionId)

    suspend fun deleteTransactionsByDate(startDate: String?, endDate: String?, transactionType: String): Int{
        return if(startDate == null || endDate == null){
            transactionDao.deleteTransaction()
        } else {
            transactionDao.deleteTransactionsByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), transactionType)
        }
    }

    suspend fun getTransactionListByDateAndAccount(startDate: String, endDate: String, accountName: String) =
            transactionDao.getTransactionListByDateAndAccount(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), accountName)

    suspend fun getTransactionListByDateAndBudget(startDate: String, endDate: String, budgetName: String) =
            transactionDao.getTransactionListByDateAndBudget(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), budgetName)
}