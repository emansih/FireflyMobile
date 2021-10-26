package xyz.hisname.fireflyiii.data.local.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import xyz.hisname.fireflyiii.repository.models.FireflyUsers
import xyz.hisname.fireflyiii.util.TypeConverterUtil


@Database(entities = [FireflyUsers::class], version = 1)
@TypeConverters(TypeConverterUtil::class)
abstract class FireflyUserDatabase: RoomDatabase()  {

    abstract fun fireflyUserDao(): FireflyUserDao

    companion object {
        @Volatile private var INSTANCE: FireflyUserDatabase? = null

        fun getInstance(context: Context): FireflyUserDatabase{
            return INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(context,
                    FireflyUserDatabase::class.java,"fireflyusers.db")
                    .setQueryExecutor(Dispatchers.IO.asExecutor())
                    .build().also { INSTANCE = it }
            }
        }
    }
}