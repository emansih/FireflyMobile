package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData

@Dao
abstract class TransactionDataDao: BaseDao<TransactionData> {

    @Query("SELECT * FROM transactions WHERE transactionType =:type")
    abstract fun getTransaction(type: String): MutableList<TransactionData>

    @Query("SELECT * FROM transactions WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :transactionType")
    abstract fun getTransaction(startDate: String?, endDate: String?,transactionType: String): MutableList<TransactionData>

    @Query("SELECT * FROM transactions order by transactionId desc limit :limit")
    abstract fun getRecentTransactions(limit: Int): MutableList<TransactionData>

}