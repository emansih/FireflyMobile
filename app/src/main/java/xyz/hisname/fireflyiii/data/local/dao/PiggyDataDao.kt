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

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData


@Dao
abstract class PiggyDataDao: BaseDao<PiggyData>{

    @Query("SELECT * FROM piggy")
    abstract suspend fun getAllPiggy(): List<PiggyData>

    @Query("SELECT COUNT(*) FROM piggy")
    abstract suspend fun getAllPiggyCount(): Int

    @Query("DELETE FROM piggy WHERE piggyId = :piggyId")
    abstract fun deletePiggyById(piggyId: Long): Int

    @Query("DELETE FROM piggy")
    abstract suspend fun deleteAllPiggyBank(): Int

    @Query("SELECT DISTINCT piggy.name FROM piggy JOIN piggyFts ON (piggy.piggyId = piggyFts.piggyId)")
    abstract fun getAllPiggyName(): Flow<List<String>>

    @Query("SELECT * FROM piggy WHERE piggyId = :piggyId")
    abstract fun getPiggyFromId(piggyId: Long): PiggyData

}