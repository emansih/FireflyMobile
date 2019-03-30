package xyz.hisname.fireflyiii.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData

@Dao
abstract class TransactionDataDao: BaseDao<TransactionData> {

    @Query("SELECT * FROM transactions")
    abstract fun getTransaction(): LiveData<MutableList<TransactionData>>

    @Query("SELECT * FROM transactions WHERE transactionType =:type")
    abstract fun getTransaction(type: String): LiveData<MutableList<TransactionData>>

    @Query("SELECT * FROM transactions WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :transactionType")
    abstract fun getTransaction(startDate: String?, endDate: String?,transactionType: String): LiveData<MutableList<TransactionData>>

    @Query("SELECT * FROM transactions WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :transactionType")
    abstract fun getTransactionList(startDate: String?, endDate: String?,transactionType: String): MutableList<TransactionData>

    // That is a really loooooooong name
    @Query("SELECT sum(amount) FROM transactions WHERE (date BETWEEN :startDate AND :endDate)" +
            " AND transactionType = :type AND currency_code = :currencyCode")
    abstract fun getTransactionsByTypeWithDateAndCurrencyCode(startDate: String?, endDate: String?,
                                                              type: String, currencyCode: String): Double

    // This too is looong
    @Query("SELECT * FROM transactions WHERE (date BETWEEN :startDate AND :endDate) " +
            "AND currency_code = :currencyCode AND source_name = :accountName")
    abstract fun getTransactionsByAccountAndCurrencyCodeAndDate(startDate: String?, endDate: String?,
                                                                currencyCode: String,
                                                                accountName: String): MutableList<TransactionData>

    @Query("SELECT sum(amount) as total FROM transactions WHERE (date BETWEEN :startDate AND " +
            ":endDate) AND currency_code =:currencyCode AND source_name =:accountName AND transactionType =:transactionType")
    abstract fun getTotalTransactionTypeByCategory(startDate: String, endDate: String, currencyCode: String,
                                  accountName: String, transactionType: String): Double

    @Query("SELECT distinct category_name FROM transactions WHERE (date BETWEEN :startDate AND" +
            " :endDate) AND currency_code = :currencyCode AND source_name = :sourceName AND transactionType =:transactionType")
    abstract fun getUniqueCategoryByDate(startDate: String, endDate: String,
                                      currencyCode: String, sourceName: String, transactionType: String): MutableList<String>

    @Query("SELECT sum(amount) as someValue FROM transactions WHERE (date BETWEEN :startDate AND :endDate) AND" +
            " currency_code = :currencyCode AND source_name = :sourceName AND transactionType =:transactionType " +
            "AND category_name =:categoryName")
    abstract fun getTransactionByDateAndCategoryAndCurrency(startDate: String, endDate: String,
                                         currencyCode: String, sourceName: String, transactionType: String,
                                                            categoryName: String): Double

    @Query("SELECT sum(amount) as someValue FROM transactions WHERE (date BETWEEN :startDate AND :endDate) AND" +
            " currency_code = :currencyCode AND source_name = :sourceName AND transactionType =:transactionType " +
            "AND category_name IS NULL")
    abstract fun getTransactionByDateAndNullCategoryAndCurrency(startDate: String, endDate: String,
                                                            currencyCode: String, sourceName: String, transactionType: String): Double


    @Query("SELECT * FROM transactions order by transactionId desc limit :limit")
    abstract fun getRecentTransactions(limit: Int): MutableList<TransactionData>

    @Query("SELECT * FROM transactions WHERE transactionId = :transactionId")
    abstract fun getTransactionById(transactionId: Long): MutableList<TransactionData>

    @Query("DELETE FROM transactions WHERE transactionId = :transactionId")
    abstract fun deleteTransactionById(transactionId: Long): Int

    @Query("DELETE FROM transactions WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :transactionType")
    abstract fun deleteTransactionsByDate(startDate: String?, endDate: String?,transactionType: String): Int

    @Query("SELECT * FROM transactions WHERE (date BETWEEN :startDate AND :endDate) AND source_name = :accountName")
    abstract fun getTransactionListByDateAndAccount(startDate: String, endDate: String, accountName: String): MutableList<TransactionData>

}