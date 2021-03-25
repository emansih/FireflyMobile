/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.data.local.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import xyz.hisname.fireflyiii.repository.models.ObjectSum
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
    @Query("SELECT transactionId FROM transactionIndexTable WHERE transactionJournalId =:journalId")
    abstract suspend fun getTransactionIdFromJournalId(journalId: Long): Long

    @Query("SELECT * FROM transactionTable WHERE transaction_journal_id =:journalId")
    abstract fun getTransactionByJournalId(journalId: Long): Transactions

    // That is a really loooooooong name
    @Query("SELECT sum(amount) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :type AND currency_code = :currencyCode")
    abstract fun getTransactionsByTypeWithDateAndCurrencyCode(startDate: String?, endDate: String?,
                                                              type: String, currencyCode: String): BigDecimal

    @Query("SELECT distinct category_name FROM transactionTable WHERE (date BETWEEN :startDate AND" +
            " :endDate) AND currency_code = :currencyCode AND source_name = :sourceName AND transactionType =:transactionType")
    abstract fun getUniqueCategoryByDate(startDate: String, endDate: String,
                                      currencyCode: String, sourceName: String, transactionType: String): MutableList<String>

    @Query("SELECT distinct budget_name FROM transactionTable WHERE (date BETWEEN :startDate AND " +
            ":endDate) AND currency_code = :currencyCode AND source_name = :sourceName AND " +
            "transactionType =:transactionType")
    abstract fun getUniqueBudgetByDate(startDate: String, endDate: String,
                                       currencyCode: String, sourceName: String, transactionType: String): MutableList<String>

    @Query("SELECT distinct budget_name FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND currency_code = :currencyCode AND transactionType =:transactionType")
    abstract fun getUniqueBudgetByDate(startDate: String, endDate: String,
                                       currencyCode: String, transactionType: String): List<String>

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND category_id =:categoryId")
    abstract suspend fun getTransactionByDateAndCategory(startDate: String, endDate: String,
                                                 categoryId: Long): List<Transactions>

    @Query("SELECT count(*) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND category_id =:categoryId ORDER BY date ASC")
    abstract suspend fun getTransactionByDateAndCategoryCount(startDate: String, endDate: String, categoryId: Long): Int

    @Query("SELECT sum(amount) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND category_id =:categoryId AND transactionType =:transactionType")
    abstract fun getTransactionValueByDateAndCategory(startDate: String, endDate: String,
                                                 transactionType: String,
                                                 categoryId: Long): BigDecimal

    @Query("SELECT sum(amount) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND currency_code = :currencyCode AND transactionType =:transactionType AND budget_name =:budgetName")
    abstract fun getTransactionByDateAndBudgetAndCurrency(startDate: String, endDate: String,
                                                          currencyCode: String,
                                                          transactionType: String,
                                                          budgetName: String): BigDecimal

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND budget_name IS NULL AND currency_code =:currencyCode")
    abstract fun getTransactionByDateAndNullBudgetAndCurrency(startDate: String, endDate: String, currencyCode: String): MutableList<Transactions>

    @Query("SELECT * FROM transactionTable INNER JOIN transactionIndexTable ON transactionTable.transaction_journal_id = transactionIndexTable.transactionJournalId ORDER BY transactionIndexTable.transactionJournalId DESC LIMIT :limit")
    abstract suspend fun getTransactionLimit(limit: Int): MutableList<Transactions>

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :transactionType ORDER BY date ASC, transaction_journal_id ASC")
    abstract suspend fun getTransactionByDate(startDate: String, endDate: String, transactionType: String): MutableList<Transactions>

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) ORDER BY date ASC, transaction_journal_id ASC")
    abstract suspend fun getTransactionByDate(startDate: String, endDate: String): MutableList<Transactions>

    @Query("SELECT * FROM transactionTable WHERE tags LIKE :tags AND (date BETWEEN :startDate AND :endDate) ORDER BY date ASC, transaction_journal_id ASC")
    abstract fun getTransactionByDateAndTag(startDate: String, endDate: String, tags: String): PagingSource<Int, Transactions>

    @Query("SELECT count(*) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND transactionType = :transactionType")
    abstract suspend fun getTransactionByDateCount(startDate: String, endDate: String, transactionType: String): Int

    @Query("SELECT count(*) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate)")
    abstract suspend fun getTransactionByDateCount(startDate: String, endDate: String): Int

    @Query("DELETE FROM transactionTable WHERE transaction_journal_id = :journalId")
    abstract suspend fun deleteTransactionByJournalId(journalId: Long): Int

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

    @Query("SELECT distinct description FROM transactionTable WHERE description LIKE :description")
    abstract fun getTransactionByDescription(description: String): PagingSource<Int, String>

    @Query("SELECT distinct description FROM transactionTable WHERE description LIKE :description")
    abstract fun getTransactionListByDescription(description: String): List<String>

    @Query("SELECT * FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND bill_id = :billId ORDER BY date ASC")
    abstract suspend fun getTransactionListByDateAndBill(billId: Long, startDate: String, endDate: String): MutableList<Transactions>

    @Query("SELECT COUNT(*) FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND bill_id = :billId ORDER BY date ASC")
    abstract suspend fun getTransactionListByDateAndBillCount(billId: Long, startDate: String, endDate: String): Long

    @Query("SELECT category_name AS objectName, SUM(amount) as objectSum FROM transactionTable WHERE source_id =:accountId AND (date BETWEEN :startDate AND :endDate) AND transactionType =:transactionType AND currency_id IN (SELECT currency_id FROM accounts WHERE accountId =:accountId) GROUP BY category_name")
    abstract suspend fun getUniqueCategoryBySourceAndDateAndType(accountId: Long, startDate: String, endDate: String, transactionType: String): List<ObjectSum>

    @Query("SELECT category_name AS objectName, SUM(amount) as objectSum FROM transactionTable WHERE destination_id =:accountId AND (date BETWEEN :startDate AND :endDate) AND transactionType =:transactionType AND currency_id IN (SELECT currency_id FROM accounts WHERE accountId =:accountId) GROUP BY category_name")
    abstract suspend fun getUniqueCategoryByDestinationAndDateAndType(accountId: Long, startDate: String, endDate: String, transactionType: String): List<ObjectSum>

    @Query("SELECT category_name AS objectName, SUM(amount) as objectSum FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND transactionType =:transactionType AND currency_code =:currencyCode GROUP BY category_name")
    abstract suspend fun getUniqueCategoryByDateAndType(startDate: String, endDate: String, currencyCode: String, transactionType: String): List<ObjectSum>

    @Query("SELECT budget_name AS objectName, SUM(amount) as objectSum FROM transactionTable WHERE source_id =:accountId AND (date BETWEEN :startDate AND :endDate) AND transactionType =:transactionType AND currency_id IN (SELECT currency_id FROM accounts WHERE accountId =:accountId) GROUP BY budget_name")
    abstract suspend fun getUniqueBudgetBySourceAndDateAndType(accountId: Long, startDate: String, endDate: String, transactionType: String): List<ObjectSum>

    @Query("SELECT budget_name AS objectName, SUM(amount) as objectSum FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND transactionType =:transactionType AND currency_code =:currencyCode GROUP BY budget_name")
    abstract suspend fun getUniqueBudgetByDateAndType(startDate: String, endDate: String, currencyCode: String, transactionType: String): List<ObjectSum>

    @Query("SELECT budget_name AS objectName, SUM(amount) as objectSum FROM transactionTable WHERE destination_id =:accountId AND (date BETWEEN :startDate AND :endDate) AND transactionType =:transactionType AND currency_id IN (SELECT currency_id FROM accounts WHERE accountId =:accountId) GROUP BY budget_name")
    abstract suspend fun getUniqueBudgetByDestinationAndDateAndType(accountId: Long, startDate: String, endDate: String, transactionType: String): List<ObjectSum>

    @Query("SELECT destination_name AS objectName, SUM(amount) as objectSum FROM transactionTable WHERE (date BETWEEN :startDate AND :endDate) AND transactionType =:transactionType AND currency_code =:currencyCode GROUP BY destination_name")
    abstract suspend fun getDestinationAccountByTypeAndDate(startDate: String, endDate: String, currencyCode: String, transactionType: String): List<ObjectSum>

    @Query("SELECT * FROM transactionTable WHERE source_id =:accountId AND (date BETWEEN :startDate AND :endDate) ORDER BY date ASC")
    abstract fun getTransactionBySourceIdAndDate(accountId: Long, startDate: String, endDate: String): PagingSource<Int, Transactions>

    @Query("SELECT * FROM transactionTable WHERE destination_id =:accountId AND (date BETWEEN :startDate AND :endDate) ORDER BY date ASC")
    abstract fun getTransactionByDestinationIdAndDate(accountId: Long, startDate: String, endDate: String): PagingSource<Int, Transactions>

    @Query("SELECT * FROM transactionIndexTable WHERE transactionId IN (SELECT transactionId FROM transactionIndexTable WHERE transactionJournalId =:journalId)")
    abstract suspend fun getTransactionSplitFromJournalId(journalId: Long): List<TransactionIndex>

    @Query("SELECT groupTitle FROM transactionIndexTable WHERE transactionId IN (SELECT transactionId FROM transactionIndexTable WHERE transactionJournalId =:journalId) LIMIT 1")
    abstract suspend fun getSplitTitle(journalId: Long): String

    // Temporary Database methods
    @Query("SELECT COUNT(*) FROM transactionIndexTable WHERE transactionId=:id")
    abstract fun getPendingTransactionFromId(id: Long): LiveData<Int>

    @Query("SELECT * FROM transactionTable INNER JOIN transactionIndexTable ON transactionTable.transaction_journal_id = transactionIndexTable.transactionJournalId  WHERE transactionIndexTable.transactionId =:id")
    abstract suspend fun getPendingTransactionFromMasterIdId(id: Long): List<Transactions>

    @Query("SELECT transactionJournalId FROM transactionIndexTable WHERE transactionId=:id")
    abstract suspend fun getTempIdFromMasterId(id: Long): List<Long>

    @Query("DELETE FROM transactionIndexTable WHERE transactionId=:id")
    abstract suspend fun deleteTempMasterId(id: Long)

    @Query("SELECT attachment FROM transactionTable WHERE transaction_journal_id =:journalId")
    abstract suspend fun getAttachmentByJournalId(journalId: Long): List<String>

    @Query("SELECT sum(amount) FROM transactionTable WHERE tags LIKE :tags AND transactionType =:transactionType AND (date BETWEEN :startDate AND :endDate) AND currency_code =:currencyCode")
    abstract suspend fun getTransactionSumByTagsAndTypeAndDateAndCurrency(tags: String, transactionType: String,
                                                               startDate: String, endDate: String, currencyCode: String): BigDecimal
}