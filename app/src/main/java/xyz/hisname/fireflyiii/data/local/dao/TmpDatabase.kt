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
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.util.TypeConverterUtil


@Database(entities = [Transactions::class, TransactionIndex::class, AccountData::class],
        version = 1, exportSchema = false)
@TypeConverters(TypeConverterUtil::class)
abstract class TmpDatabase: RoomDatabase() {

    abstract fun transactionDataDao(): TransactionDataDao

    companion object {
        @Volatile private var INSTANCE: TmpDatabase? = null

        fun getInstance(context: Context): TmpDatabase{
            return INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(context,
                        TmpDatabase::class.java,"temp-"  + Constants.DB_NAME)
                        .setQueryExecutor(Dispatchers.IO.asExecutor())
                        .build().also { INSTANCE = it }
            }
        }
    }

}