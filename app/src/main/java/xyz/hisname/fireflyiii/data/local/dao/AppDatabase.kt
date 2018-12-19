package xyz.hisname.fireflyiii.data.local.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.budget.BudgetData
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.util.GsonConverterUtil
import androidx.sqlite.db.SupportSQLiteDatabase



@Database(entities = [PiggyData::class, BillData::class, AccountData::class, CurrencyData::class,
    TransactionData::class, CategoryData::class, BudgetData::class],
        version = 2,exportSchema = false)
@TypeConverters(GsonConverterUtil::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun billDataDao(): BillDataDao
    abstract fun piggyDataDao(): PiggyDataDao
    abstract fun accountDataDao(): AccountsDataDao
    abstract fun currencyDataDao(): CurrencyDataDao
    abstract fun transactionDataDao(): TransactionDataDao
    abstract fun categoryDataDao(): CategoryDataDao
    abstract fun budgetDataDao(): BudgetDataDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(context,
                        AppDatabase::class.java, Constants.DB_NAME)
                        .fallbackToDestructiveMigration()
                        .addMigrations(MIGRATION_1_2)
                        .build().also { INSTANCE = it }
            }
        }

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE budget")
            }
        }

        fun clearDb(context: Context) = getInstance(context).clearAllTables()

    }
}