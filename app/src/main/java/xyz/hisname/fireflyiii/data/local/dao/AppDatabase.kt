package xyz.hisname.fireflyiii.data.local.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.budget.BudgetData
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListFts
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.Spent
import xyz.hisname.fireflyiii.repository.models.budget.limits.BudgetLimitData
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.repository.models.category.CategoryFts
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyFts
import xyz.hisname.fireflyiii.repository.models.tags.TagsData
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.util.TypeConverterUtil


@Database(entities = [PiggyData::class, PiggyFts::class, BillData::class, AccountData::class, CurrencyData::class,
    Transactions::class, TransactionIndex::class, CategoryData::class, CategoryFts::class, BudgetData::class,
    BudgetListData::class, BudgetListFts::class, TagsData::class, AttachmentData::class, Spent::class, BudgetLimitData::class],
        version = 19, exportSchema = false)
@TypeConverters(TypeConverterUtil::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun billDataDao(): BillDataDao
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

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(context,
                        AppDatabase::class.java, Constants.DB_NAME)
                        .setQueryExecutor(Dispatchers.IO.asExecutor())
                        .fallbackToDestructiveMigration()
                        .build().also { INSTANCE = it }
            }
        }

        fun clearDb(context: Context) = getInstance(context).clearAllTables()

    }
}