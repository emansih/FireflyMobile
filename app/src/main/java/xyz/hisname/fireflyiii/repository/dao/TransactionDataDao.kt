package xyz.hisname.fireflyiii.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData

@Dao
abstract class TransactionDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addTransaction(vararg transactionData: TransactionData)

    @Query("SELECT * FROM transactions WHERE transactionType =:type")
    abstract fun getTransaction(type: String): LiveData<MutableList<TransactionData>>

    @Query("SELECT * FROM transactions WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :transactionType")
    abstract fun getTransaction(startDate: String?, endDate: String?,transactionType: String): LiveData<MutableList<TransactionData>>

    @Query("SELECT * FROM transactions order by transactionId desc limit :limit")
    abstract fun getRecentTransactions(limit: Int): LiveData<MutableList<TransactionData>>

}