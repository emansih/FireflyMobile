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

package xyz.hisname.fireflyiii.ui

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.bills.BillRepository
import xyz.hisname.fireflyiii.repository.bills.BillsPaidRepository
import xyz.hisname.fireflyiii.util.DateTimeUtil

class HomeViewModel(application: Application): BaseViewModel(application) {

    private val billsService = genericService().create(BillsService::class.java)
    private val billDataDao = AppDatabase.getInstance(application).billDataDao()
    private val billPaidDao = AppDatabase.getInstance(application).billPaidDao()
    private val billPayDao = AppDatabase.getInstance(application).billPayDao()
    private val billRepository = BillRepository(billDataDao, billsService)
    private val billPaidRepository = BillsPaidRepository(billPaidDao, billsService)

    fun getNoOfBillsDueToday(): LiveData<Int> {
        val count = MutableLiveData<Int>()
        viewModelScope.launch(Dispatchers.IO){
            val billDue = billRepository.getBillDueFromDate(DateTimeUtil.getTodayDate())
            val billPaidId = billPaidRepository.getBillPaidByDate(DateTimeUtil.getTodayDate(),
                    DateTimeUtil.getTodayDate(), billPayDao)
            val billDueId = arrayListOf<Long>()
            billDue.forEach {  billData ->
                billDueId.add(billData.billId)
            }
            val billIdDifference = billDueId.minus(billPaidId)
            count.postValue(billIdDifference.size)
        }
        return count
    }
}