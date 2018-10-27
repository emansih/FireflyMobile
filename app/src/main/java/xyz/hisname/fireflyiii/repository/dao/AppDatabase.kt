package xyz.hisname.fireflyiii.repository.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.util.GsonConverterUtil

@Database(entities = [PiggyData::class, BillData::class, AccountData::class, CurrencyData::class], version = 1,exportSchema = false)
@TypeConverters(GsonConverterUtil::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun billDataDao(): BillDataDao
    abstract fun piggyDataDao(): PiggyDataDao
    abstract fun accountDataDao(): AccountsDataDao
    abstract fun currencyDataDao(): CurrencyDataDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? {
            return INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(context,
                        AppDatabase::class.java, Constants.DB_NAME)
                        .build().also { INSTANCE = it }
            }
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}