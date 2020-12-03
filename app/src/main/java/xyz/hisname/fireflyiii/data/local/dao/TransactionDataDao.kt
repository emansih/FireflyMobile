package xyz.hisname.fireflyiii.data.local.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import java.math.BigDecimal

@Dao
abstract class TransactionDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(vararg obj: Transactions)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(vararg obj: TransactionIndex)

    @Query("SELECT * FROM transactionTable WHERE transactionType = :type AND currency_code =:currencyCode")
    abstract suspend fun getTransactionListWithCurrency(type: String, currencyCode: String): MutableList<Transactions>

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :transactionType AND currency_code =:currencyCode ORDER BY date ASC")
    abstract suspend fun getTransactionListWithCurrencyAndDate(startDate: String, endDate: String,transactionType: String, currencyCode: String): MutableList<Transactions>

    @Query("SELECT count(*) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :transactionType AND currency_code =:currencyCode")
    abstract suspend fun getTransactionListWithCurrencyAndDateCount(startDate: String, endDate: String,transactionType: String, currencyCode: String): Int

    // Takes transaction id as parameter and return transaction journal id
    @Query("SELECT transactionId FROM transactionIndexTable WHERE transactionJournalId = :journalId")
    abstract fun getTransactionIdFromJournalId(journalId: Long): Long

    @Query("SELECT * FROM transactionTable WHERE transaction_journal_id = :journalId")
    abstract fun getTransactionByJournalId(journalId: Long): MutableList<Transactions>

    // That is a really loooooooong name
    @Query("SELECT sum(amount) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate)" +
            " AND transactionType = :type AND currency_code = :currencyCode")
    abstract fun getTransactionsByTypeWithDateAndCurrencyCode(startDate: String?, endDate: String?,
                                                              type: String, currencyCode: String): BigDecimal
    // This too is looong
    @Query("SELECT sum(amount) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) " +
            "AND currency_code = :currencyCode AND source_name = :accountName")
    abstract fun getTransactionsByAccountAndCurrencyCodeAndDate(startDate: String?, endDate: String?,
                                                                currencyCode: String,
                                                                accountName: String): Double

    @Query("SELECT sum(amount) as total FROM transactionTable WHERE (date BETWEEN :startDate AND " +
            ":endDate) AND currency_code =:currencyCode AND source_name =:accountName AND transactionType =:transactionType")
    abstract fun getTotalTransactionType(startDate: String, endDate: String, currencyCode: String,
                                  accountName: String, transactionType: String): Double

    @Query("SELECT sum(amount) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND currency_code =:currencyCode AND transactionType =:transactionType")
    abstract fun getTotalTransactionType(startDate: String, endDate: String, currencyCode: String, transactionType: String): BigDecimal

    @Query("SELECT distinct category_name FROM transactionTable WHERE (date BETWEEN :startDate AND" +
            " :endDate) AND currency_code = :currencyCode AND source_name = :sourceName AND transactionType =:transactionType")
    abstract fun getUniqueCategoryByDate(startDate: String, endDate: String,
                                      currencyCode: String, sourceName: String, transactionType: String): MutableList<String>

    @Query("SELECT distinct budget_name FROM transactionTable WHERE (date BETWEEN :startDate AND " +
            ":endDate) AND currency_code = :currencyCode AND source_name = :sourceName AND " +
            "transactionType =:transactionType")
    abstract fun getUniqueBudgetByDate(startDate: String, endDate: String,
                                       currencyCode: String, sourceName: String, transactionType: String): MutableList<String>

    @Query("SELECT distinct budget_name FROM transactionTable WHERE (date BETWEEN :startDate AND " +
            ":endDate) AND currency_code = :currencyCode AND transactionType =:transactionType")
    abstract fun getUniqueBudgetByDate(startDate: String, endDate: String,
                                       currencyCode: String, transactionType: String): List<String>

    @Query("SELECT sum(amount) as someValue FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND" +
            " currency_code = :currencyCode AND source_name = :sourceName AND transactionType =:transactionType " +
            "AND category_name =:categoryName")
    abstract fun getTransactionByDateAndCategoryAndCurrency(startDate: String, endDate: String,
                                                            currencyCode: String, sourceName: String, transactionType: String,
                                                            categoryName: String): Double

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND category_id =:categoryId")
    abstract suspend fun getTransactionByDateAndCategory(startDate: String, endDate: String,
                                                 categoryId: Long): List<Transactions>

    @Query("SELECT count(*) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND category_id =:categoryId ORDER BY date ASC")
    abstract suspend fun getTransactionByDateAndCategoryCount(startDate: String, endDate: String, categoryId: Long): Int

    @Query("SELECT sum(amount) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) " +
            "AND category_id =:categoryId AND transactionType =:transactionType")
    abstract fun getTransactionValueByDateAndCategory(startDate: String, endDate: String,
                                                 transactionType: String,
                                                 categoryId: Long): BigDecimal

    @Query("SELECT sum(amount) as someValue FROM transactionTable WHERE (date BETWEEN :startDate " +
            "AND :endDate) AND currency_code = :currencyCode AND source_name = :sourceName AND " +
            "transactionType =:transactionType AND budget_name =:budgetName")
    abstract fun getTransactionByDateAndBudgetAndCurrency(startDate: String, endDate: String,
                                                          currencyCode: String, sourceName: String,
                                                          transactionType: String,
                                                          budgetName: String): Double

    @Query("SELECT sum(amount) as someValue FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND currency_code = :currencyCode AND transactionType =:transactionType AND budget_name =:budgetName")
    abstract fun getTransactionByDateAndBudgetAndCurrency(startDate: String, endDate: String,
                                                          currencyCode: String,
                                                          transactionType: String,
                                                          budgetName: String): BigDecimal

    @Query("SELECT sum(amount) as someValue FROM transactionTable WHERE (date BETWEEN :startDate " +
            "AND :endDate) AND currency_code = :currencyCode AND source_name = :sourceName AND " +
            "transactionType =:transactionType AND budget_name IS NULL")
    abstract fun getTransactionByDateAndNullBudgetAndCurrency(startDate: String, endDate: String,
                                                              currencyCode: String, sourceName: String,
                                                              transactionType: String): Double

    @Query("SELECT sum(amount) as someValue FROM transactionTable WHERE (date BETWEEN :startDate " +
            "AND :endDate) AND currency_code = :currencyCode AND transactionType =:transactionType AND" +
           " budget_name IS NULL")
    abstract fun getTransactionAmountByDateAndNullBudgetAndCurrency(startDate: String, endDate: String,
                                                              currencyCode: String,
                                                              transactionType: String): Double

    @Query("SELECT sum(amount) as someValue FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND" +
            " currency_code = :currencyCode AND source_name = :sourceName AND transactionType =:transactionType " +
            "AND category_name IS NULL")
    abstract fun getTransactionByDateAndNullCategoryAndCurrency(startDate: String, endDate: String,
                                                            currencyCode: String, sourceName: String, transactionType: String): Double

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND budget_name IS NULL AND currency_code =:currencyCode")
    abstract fun getTransactionByDateAndNullBudgetAndCurrency(startDate: String, endDate: String, currencyCode: String): MutableList<Transactions>

    @Query("SELECT * FROM transactionTable INNER JOIN transactionIndexTable ON transactionTable.transaction_journal_id = transactionIndexTable.transactionJournalId ORDER BY transactionIndexTable.transactionJournalId DESC LIMIT :limit")
    abstract suspend fun getTransactionLimit(limit: Int): MutableList<Transactions>

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :transactionType ORDER BY date ASC")
    abstract suspend fun getTransactionByDate(startDate: String, endDate: String, transactionType: String): MutableList<Transactions>

    @Query("SELECT count(*) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :transactionType")
    abstract suspend fun getTransactionByDateCount(startDate: String, endDate: String, transactionType: String): Int

    @Query("SELECT count(*) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate)")
    abstract suspend fun getTransactionByDateCount(startDate: String, endDate: String): Int

    @Query("SELECT * FROM transactionTable WHERE transaction_journal_id = :journalId")
    abstract fun getTransactionFromJournalId(journalId: Long): MutableList<Transactions>

    @Query("DELETE FROM transactionTable WHERE transaction_journal_id = :journalId")
    abstract fun deleteTransactionByJournalId(journalId: Long): Int

    @Query("DELETE FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :transactionType AND isPending IS NOT :isPending")
    abstract suspend fun deleteTransactionsByDate(startDate: String?, endDate: String?,transactionType: String, isPending: Boolean = true): Int

    @Query("DELETE FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND isPending IS NOT :isPending")
    abstract suspend fun deleteTransactionsByDate(startDate: String?, endDate: String?, isPending: Boolean = true): Int

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND source_name = :accountName")
    abstract fun getTransactionListByDateAndAccount(startDate: String, endDate: String, accountName: String): MutableList<Transactions>

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND budget_name = :budgetName AND currency_code =:currencyCode ORDER BY date ASC")
    abstract suspend fun getTransactionListByDateAndBudget(startDate: String, endDate: String, budgetName: String, currencyCode: String): MutableList<Transactions>

    @Query("DELETE FROM transactionTable WHERE isPending IS NOT :isPending")
    abstract fun deleteTransaction(isPending: Boolean = true): Int

    @Query("SELECT description FROM transactionTable WHERE description LIKE :description")
    abstract fun getTransactionByDescription(description: String): Flow<MutableList<String>>

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND bill_id = :billId ORDER BY date ASC")
    abstract suspend fun getTransactionListByDateAndBill(billId: Long, startDate: String, endDate: String): MutableList<Transactions>

    @Query("SELECT COUNT(*) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND bill_id = :billId ORDER BY date ASC")
    abstract suspend fun getTransactionListByDateAndBillCount(billId: Long, startDate: String, endDate: String): Long

}