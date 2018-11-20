package xyz.hisname.fireflyiii.repository.transaction

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData

class TransactionRepository(private val transactionDao: TransactionDataDao) {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertTransaction(transaction: TransactionData){
        transactionDao.insert(transaction)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun allWithdrawal(startDate: String?, endDate: String?) =
            transactionDao.getTransactionsByTypeWithDate(startDate, endDate, "Withdrawal")

    fun withdrawalList(startDate: String?, endDate: String?): LiveData<MutableList<TransactionData>>{
        return if(startDate.isNullOrBlank() || endDate.isNullOrBlank()){
            transactionDao.getTransaction("Withdrawal")
        } else {
            transactionDao.getTransaction(startDate, endDate, "Withdrawal")
        }
    }

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
    suspend fun allDeposit(startDate: String?, endDate: String?) =
            transactionDao.getTransactionsByTypeWithDate(startDate, endDate, "Deposit")
    
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun recentTransactions(limit: Int) = transactionDao.getRecentTransactions(limit)


}