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
import androidx.room.Transaction
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.bills.BillPaidDates

@Dao
abstract class BillPaidDao: BaseDao<BillPaidDates> {

    @Query("DELETE FROM billPaidList")
    abstract suspend fun deleteAllPaidList(): Int

    @Query("DELETE FROM billPaidList WHERE id =:billId")
    abstract suspend fun deleteByBillId(billId: Long): Int

    @Query("SELECT * FROM billPaidList WHERE id =:billId  AND strftime('%s', date) BETWEEN strftime('%s', :startDate) AND strftime('%s', :endDate)")
    abstract suspend fun getBillsPaidFromIdAndDate(billId: Long, startDate: String, endDate: String): List<BillPaidDates>

    @Query("SELECT id FROM billPaidList WHERE strftime('%s', date) BETWEEN strftime('%s', :startDate) AND strftime('%s', :endDate)")
    abstract suspend fun getBillsPaidFromAndDate(startDate: String, endDate: String): List<Long>

    @Query("DELETE FROM billPaidList WHERE  (date BETWEEN :startDate AND :endDate)")
    abstract suspend fun deletePaidByDate(startDate: String, endDate: String): Int


    @Transaction
    open suspend fun deleteAndInsert(startDate: String, endDate: String, list: List<BillData>){
        deletePaidByDate(startDate, endDate)
        list.forEach {  billData ->
            billData.billAttributes.paid_dates.forEach { billPaid ->
                insert(BillPaidDates(
                        id = billData.billId, transaction_group_id = billPaid.transaction_group_id,
                        transaction_journal_id = billPaid.transaction_journal_id,
                        date = billPaid.date
                ))
            }
        }
    }
}