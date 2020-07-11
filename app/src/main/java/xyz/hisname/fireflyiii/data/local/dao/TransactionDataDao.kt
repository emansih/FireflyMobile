package xyz.hisname.fireflyiii.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import java.math.BigDecimal

@Dao
abstract class TransactionDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(vararg obj: Transactions)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(vararg obj: TransactionIndex)

    @Query("SELECT * FROM transactionTable WHERE transactionType = :type")
    abstract fun getTransactionList(type: String): MutableList<Transactions>

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :transactionType")
    abstract fun getTransaction(startDate: String?, endDate: String?,transactionType: String): LiveData<MutableList<Transactions>>

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :transactionType")
    abstract fun getTransactionList(startDate: String?, endDate: String?,transactionType: String): MutableList<Transactions>

    // Takes transaction id as parameter and return transaction journal id
    @Query("SELECT transactionId FROM transactionIndexTable WHERE transactionJournalId = :journalId")
    abstract fun getTransactionIdFromJournalId(journalId: Long): Long

    @Query("SELECT * FROM transactionTable WHERE transaction_journal_id = :journalId")
    abstract fun getTransactionByJournalId(journalId: Long): MutableList<Transactions>

    // That is a really loooooooong name
    @Query("SELECT sum(amount) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate)" +
            " AND transactionType = :type AND currency_code = :currencyCode")
    abstract fun getTransactionsByTypeWithDateAndCurrencyCode(startDate: String?, endDate: String?,
                                                              type: String, currencyCode: String): Double

    // This too is looong
    @Query("SELECT sum(amount) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) " +
            "AND currency_code = :currencyCode AND source_name = :accountName")
    abstract fun getTransactionsByAccountAndCurrencyCodeAndDate(startDate: String?, endDate: String?,
                                                                currencyCode: String,
                                                                accountName: String): BigDecimal

    @Query("SELECT sum(amount) as total FROM transactionTable WHERE (date BETWEEN :startDate AND " +
            ":endDate) AND currency_code =:currencyCode AND source_name =:accountName AND transactionType =:transactionType")
    abstract fun getTotalTransactionType(startDate: String, endDate: String, currencyCode: String,
                                  accountName: String, transactionType: String): Double

    @Query("SELECT sum(amount) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) " +
            "AND currency_code =:currencyCode AND transactionType =:transactionType")
    abstract fun getTotalTransactionType(startDate: String, endDate: String, currencyCode: String,
                                         transactionType: String): Double

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
                                       currencyCode: String, transactionType: String): MutableList<String?>

    @Query("SELECT sum(amount) as someValue FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND" +
            " currency_code = :currencyCode AND source_name = :sourceName AND transactionType =:transactionType " +
            "AND category_name =:categoryName")
    abstract fun getTransactionByDateAndCategoryAndCurrency(startDate: String, endDate: String,
                                                            currencyCode: String, sourceName: String, transactionType: String,
                                                            categoryName: String): Double

    @Query("SELECT sum(amount) as someValue FROM transactionTable WHERE (date BETWEEN :startDate " +
            "AND :endDate) AND currency_code = :currencyCode AND source_name = :sourceName AND " +
            "transactionType =:transactionType AND budget_name =:budgetName")
    abstract fun getTransactionByDateAndBudgetAndCurrency(startDate: String, endDate: String,
                                                          currencyCode: String, sourceName: String,
                                                          transactionType: String,
                                                          budgetName: String): Double

    @Query("SELECT sum(amount) as someValue FROM transactionTable WHERE (date BETWEEN :startDate " +
            "AND :endDate) AND currency_code = :currencyCode AND " +
            "transactionType =:transactionType AND budget_name =:budgetName")
    abstract fun getTransactionByDateAndBudgetAndCurrency(startDate: String, endDate: String,
                                                          currencyCode: String,
                                                          transactionType: String,
                                                          budgetName: String): Double

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

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND budget_name IS NULL")
    abstract fun getTransactionByDateAndNullBudgetAndCurrency(startDate: String, endDate: String): MutableList<Transactions>

    @Query("SELECT * FROM transactionIndexTable order by transactionId desc limit :limit")
    abstract fun getTransactionLimit(limit: Int): MutableList<TransactionIndex>

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :transactionType limit :limit")
    abstract fun getTransactionLimitByDate(startDate: String, endDate: String, transactionType: String, limit: Int): MutableList<Transactions>

    @Query("SELECT * FROM transactionTable WHERE transactionType = :transactionType limit :limit")
    abstract fun getTransactionLimitByType(transactionType: String, limit: Int): MutableList<Transactions>

    @Query("SELECT * FROM transactionTable WHERE transaction_journal_id = :journalId")
    abstract fun getTransactionFromJournalId(journalId: Long): MutableList<Transactions>

    @Query("DELETE FROM transactionTable WHERE transaction_journal_id = :journalId")
    abstract fun deleteTransactionByJournalId(journalId: Long): Int

   @Query("DELETE FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :transactionType")
    abstract fun deleteTransactionsByDate(startDate: String?, endDate: String?,transactionType: String): Int

    @Query("DELETE FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate)")
    abstract fun deleteTransactionsByDate(startDate: String?, endDate: String?): Int

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND source_name = :accountName")
    abstract fun getTransactionListByDateAndAccount(startDate: String, endDate: String, accountName: String): MutableList<Transactions>

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND budget_name = :budgetName")
    abstract fun getTransactionListByDateAndBudget(startDate: String, endDate: String, budgetName: String): MutableList<Transactions>

    @Query("DELETE FROM transactionTable")
    abstract fun deleteTransaction(): Int
}