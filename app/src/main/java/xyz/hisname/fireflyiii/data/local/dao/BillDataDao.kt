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

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.repository.models.bills.BillData

@Dao
abstract class BillDataDao: BaseDao<BillData>{

    @Query("SELECT * FROM bills")
    abstract suspend fun getBill(): List<BillData>

    @Query("SELECT COUNT(*) FROM bills")
    abstract suspend fun getBillCount(): Long

    @Query("DELETE FROM bills WHERE billId = :billId")
    abstract fun deleteBillById(billId: Long): Int

    @Query("SELECT * FROM bills WHERE billId = :billId")
    abstract fun getBillById(billId: Long): BillData

    @Query("DELETE FROM bills")
    abstract suspend fun deleteAllBills(): Int

    @Query("SELECT DISTINCT(name) FROM bills")
    abstract fun getAllBillName(): Flow<List<String>>
}
