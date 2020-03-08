package xyz.hisname.fireflyiii.data.local.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.budget.BudgetData
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListFts
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.repository.models.category.CategoryFts
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyFts
import xyz.hisname.fireflyiii.repository.models.tags.TagsData
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.util.GsonConverterUtil



@Database(entities = [PiggyData::class, PiggyFts::class, BillData::class, AccountData::class, CurrencyData::class,
    Transactions::class, TransactionIndex::class, CategoryData::class, CategoryFts::class, BudgetData::class,
    BudgetListData::class, BudgetListFts::class, TagsData::class, AttachmentData::class],
        version = 13,exportSchema = false)
@TypeConverters(GsonConverterUtil::class)
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

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(context,
                        AppDatabase::class.java, Constants.DB_NAME)
                        .addMigrations(object : Migration(8, 9){
                            override fun migrate(database: SupportSQLiteDatabase) {
                                // Category
                                database.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `categoryFts` USING FTS4(`categoryId`, `name`, content=`category`)")
                                database.execSQL("INSERT INTO categoryFts(categoryFts) VALUES ('rebuild')")
                                // Budget
                                database.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `budgetListFts` USING FTS4(`budgetListId`, `name`, content=`budget_list`)")
                                database.execSQL("INSERT INTO budgetListFts(budgetListFts) VALUES ('rebuild')")
                                // Piggy Bank
                                database.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `piggyFts` USING FTS4(`piggyId`, `name`, content=`piggy`)")
                                database.execSQL("INSERT INTO piggyFts(piggyFts) VALUES ('rebuild')")
                            }
                        })
                        .fallbackToDestructiveMigration()
                        .build().also { INSTANCE = it }
            }
        }

        fun clearDb(context: Context) = getInstance(context).clearAllTables()

    }
}