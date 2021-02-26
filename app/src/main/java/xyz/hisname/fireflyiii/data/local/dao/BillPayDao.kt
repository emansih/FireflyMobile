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
import xyz.hisname.fireflyiii.repository.models.bills.BillPayDates


@Dao
abstract class BillPayDao: BaseDao<BillPayDates> {

    @Query("DELETE FROM billPayList")
    abstract suspend fun deleteAllPayList(): Int

    @Query("DELETE FROM billPayList WHERE (payDates BETWEEN :startDate AND :endDate)")
    abstract suspend fun deletePayListByDate(startDate: String, endDate: String): Int

    @Query("SELECT * FROM billPayList WHERE id =:billId AND strftime('%s', payDates) BETWEEN strftime('%s', :startDate) AND strftime('%s', :endDate)")
    abstract suspend fun getBillByDateAndId(billId: Long, startDate: String, endDate: String): List<BillPayDates>

    @Query("SELECT COUNT(payDates) FROM billPayList WHERE  (payDates BETWEEN :startDate AND :endDate)")
    abstract suspend fun getBillCountByDate(startDate: String, endDate: String): Int
}
