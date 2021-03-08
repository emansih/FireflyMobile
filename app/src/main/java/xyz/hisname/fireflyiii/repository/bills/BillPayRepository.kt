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
import xyz.hisname.fireflyiii.data.local.dao.BillDataDao
import xyz.hisname.fireflyiii.data.local.dao.BillPayDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.bills.BillPayDates
import java.lang.Exception
import java.time.LocalDate

class BillPayRepository(private val billPayDao: BillPayDao,
                        private val billsService: BillsService) {


    suspend fun getPaidDatesFromBillId(billId: Long, startDate: String, endDate: String): List<BillPayDates>{
        try {
            val networkCall = billsService.getBillById(billId, startDate, endDate)
            val responseBody = networkCall.body()
            if(responseBody != null && networkCall.isSuccessful){
                responseBody.data.billAttributes.pay_dates.forEach { localDate ->
                    billPayDao.insert(BillPayDates(id = billId, payDates = LocalDate.parse(localDate)))
                }
            }
        } catch (exception: Exception){ }
        return billPayDao.getBillByDateAndId(billId,startDate, endDate)
    }

}