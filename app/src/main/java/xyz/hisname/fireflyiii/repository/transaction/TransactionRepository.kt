package xyz.hisname.fireflyiii.repository.transaction

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData

class TransactionRepository(private val transactionDao: TransactionDataDao) {

    val allTransaction = transactionDao.getTransaction()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertTransaction(transaction: TransactionData){
        transactionDao.insert(transaction)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun allWithdrawalWithCurrencyCode(startDate: String?, endDate: String?, currencyCode: String) =
            transactionDao.getTransactionsByTypeWithDateAndCurrencyCode(startDate, endDate, "Withdrawal", currencyCode)

    fun withdrawalList(startDate: String?, endDate: String?): LiveData<MutableList<TransactionData>>{
        return if(startDate.isNullOrBlank() || endDate.isNullOrBlank()){
            transactionDao.getTransaction("Withdrawal")
        } else {
            transactionDao.getTransaction(startDate, endDate, "Withdrawal")
        }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun allDepositWithCurrencyCode(startDate: String?, endDate: String?, currencyCode: String) =
            transactionDao.getTransactionsByTypeWithDateAndCurrencyCode(startDate, endDate, "Deposit", currencyCode)

    fun depositList(startDate: String?, endDate: String?): LiveData<MutableList<TransactionData>> {
       return if (startDate.isNullOrBlank() || endDate.isNullOrBlank()) {
           transactionDao.getTransaction("Deposit")
       } else {
           transactionDao.getTransaction(startDate, endDate, "Deposit")
       }
   }

    fun transferList(startDate: String?, endDate: String?): LiveData<MutableList<TransactionData>> {
        return if (startDate.isNullOrBlank() || endDate.isNullOrBlank()) {
            transactionDao.getTransaction("Transfer")
        } else {
            transactionDao.getTransaction(startDate, endDate, "Transfer")
        }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun recentTransactions(limit: Int) = transactionDao.getRecentTransactions(limit)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getTransactionById(transactionId: Long) = transactionDao.getTransactionById(transactionId)

}