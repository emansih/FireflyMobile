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

package xyz.hisname.fireflyiii.repository.bills

import timber.log.Timber
import xyz.hisname.fireflyiii.data.local.dao.BillPaidDao
import xyz.hisname.fireflyiii.data.local.dao.BillPayDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.bills.BillPaidDates
import java.lang.Exception

class BillsPaidRepository(private val billsPaidDao: BillPaidDao,
                          private val billsService: BillsService) {

    suspend fun getBillPaidById(billId: Long, startDate: String, endDate: String): List<BillPaidDates>{
        try {
            val networkCall = billsService.getBillById(billId, startDate, endDate)
            val responseBody = networkCall.body()
            if(responseBody != null && networkCall.isSuccessful){
                billsPaidDao.deleteByBillId(billId)
                responseBody.data.billAttributes.paid_dates.forEach {  billPaid ->
                    billsPaidDao.insert(BillPaidDates(
                            id = billId, transaction_group_id = billPaid.transaction_group_id,
                            transaction_journal_id = billPaid.transaction_journal_id,
                            date = billPaid.date
                    ))
                }
            }
        } catch (exception: Exception){ }
        return billsPaidDao.getBillsPaidFromIdAndDate(billId, startDate, endDate)
    }

    suspend fun getBillPaidByDate(startDate: String, endDate: String, billPayDao: BillPayDao): List<Long>{
        try {
            val billList = arrayListOf<BillData>()
            val networkCall = billsService.getPaginatedBills(1, startDate, endDate)
            val responseBody = networkCall.body()
            if(responseBody != null && networkCall.isSuccessful){
                billList.addAll(responseBody.data)
                if (responseBody.meta.pagination.current_page != responseBody.meta.pagination.total_pages) {
                    for(pagination in 2..responseBody.meta.pagination.total_pages){
                        val networkBody = billsService.getPaginatedBills(pagination).body()
                        if(networkBody != null){
                            billList.addAll(networkBody.data)
                        }
                    }
                }
                billsPaidDao.deleteAndInsert(startDate, endDate, billList, billPayDao)
            }
        } catch (exception: Exception){ }
        return billsPaidDao.getBillsPaidFromAndDate(startDate, endDate)
    }
}