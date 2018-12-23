package xyz.hisname.fireflyiii.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData

@Dao
abstract class TransactionDataDao: BaseDao<TransactionData> {

    @Query("SELECT * FROM transactions WHERE transactionType =:type")
    abstract fun getTransaction(type: String): LiveData<MutableList<TransactionData>>

    @Query("SELECT * FROM transactions WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :transactionType")
    abstract fun getTransaction(startDate: String?, endDate: String?,transactionType: String): LiveData<MutableList<TransactionData>>

    @Query("SELECT * FROM transactions WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :type")
    abstract fun getTransactionsByTypeWithDate(startDate: String?, endDate: String?, type: String): MutableList<TransactionData>

    // That is a really loooooooong name
    @Query("SELECT * FROM transactions WHERE (date BETWEEN :startDate AND :endDate) " +
            "AND transactionType = :type AND currency_code = :currencyCode")
    abstract fun getTransactionsByTypeWithDateAndCurrencyCode(startDate: String?, endDate: String?, type: String, currencyCode: String): MutableList<TransactionData>

    @Query("SELECT * FROM transactions order by transactionId desc limit :limit")
    abstract fun getRecentTransactions(limit: Int): MutableList<TransactionData>

}