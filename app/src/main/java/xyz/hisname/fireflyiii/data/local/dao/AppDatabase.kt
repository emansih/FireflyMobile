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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.bills.BillPaidDates
import xyz.hisname.fireflyiii.repository.models.bills.BillPayDates
import xyz.hisname.fireflyiii.repository.models.budget.BudgetData
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListFts
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.Spent
import xyz.hisname.fireflyiii.repository.models.budget.limits.BudgetLimitData
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.repository.models.category.CategoryFts
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyRemoteKeys
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyFts
import xyz.hisname.fireflyiii.repository.models.tags.TagsData
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.util.TypeConverterUtil
import java.util.*


@Database(entities = [PiggyData::class, PiggyFts::class, BillData::class, AccountData::class, CurrencyData::class,
    Transactions::class, TransactionIndex::class, CategoryData::class, CategoryFts::class, BudgetData::class,
    BudgetListData::class, BudgetListFts::class, TagsData::class, AttachmentData::class,
    Spent::class, BudgetLimitData::class, BillPaidDates::class, BillPayDates::class, CurrencyRemoteKeys::class],
        version = 30, exportSchema = false)
@TypeConverters(TypeConverterUtil::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun billDataDao(): BillDataDao
    abstract fun billPaidDao(): BillPaidDao
    abstract fun billPayDao(): BillPayDao
    abstract fun piggyDataDao(): PiggyDataDao
    abstract fun accountDataDao(): AccountsDataDao
    abstract fun currencyDataDao(): CurrencyDataDao
    abstract fun transactionDataDao(): TransactionDataDao
    abstract fun categoryDataDao(): CategoryDataDao
    abstract fun budgetDataDao(): BudgetDataDao
    abstract fun budgetListDataDao(): BudgetListDataDao
    abstract fun tagsDataDao(): TagsDataDao
    abstract fun attachmentDataDao(): AttachmentDataDao
    abstract fun spentDataDao(): SpentDataDao
    abstract fun budgetLimitDao(): BudgetLimitDao
    abstract fun currencyRemoteKeysDao(): CurrencyKeyDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, randomHash: UUID): AppDatabase {
            return INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(context,
                    AppDatabase::class.java, "$randomHash-photuris.db"
                )
                    .setQueryExecutor(Dispatchers.IO.asExecutor())
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }

        fun destroyInstance(){
            INSTANCE = null
        }

        fun clearDb(context: Context, randomHash: UUID) =
            getInstance(context, randomHash).clearAllTables()
    }
}