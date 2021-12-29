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

        fun getInstance(context: Context, uuid: String): TmpDatabase{
            return INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(context,
                        TmpDatabase::class.java,"$uuid-temp-"  + Constants.DB_NAME)
                        .setQueryExecutor(Dispatchers.IO.asExecutor())
                        .build().also { INSTANCE = it }
            }
        }
    }

}