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

package xyz.hisname.fireflyiii.ui.bills.list

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.bills.BillPageSource
import xyz.hisname.fireflyiii.repository.bills.BillRepository
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.bill.DeleteBillWorker

class ListBillViewModel(application: Application): BaseViewModel(application) {

    private val billService = genericService().create(BillsService::class.java)
    private val billDataDao = AppDatabase.getInstance(application).billDataDao()
    private val billRepository = BillRepository(billDataDao, billService)

    fun getBillList(): LiveData<PagingData<BillData>> {
        return Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)){
            BillPageSource(billService, billDataDao)
        }.flow.cachedIn(viewModelScope).asLiveData()
    }

    fun deleteBillById(billId: String): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO) {
            val billAttributes = billRepository.getBillById(billId.toLong())
            if (billAttributes.billId != 0L) {
                // Since onDraw() is being called multiple times, we check if the bill exists locally in the DB.
                when (billRepository.deleteBillById(billId.toLong())) {
                    HttpConstants.FAILED -> {
                        isDeleted.postValue(false)
                        DeleteBillWorker.initPeriodicWorker(billId.toLong(), getApplication())
                    }
                    HttpConstants.UNAUTHORISED -> {
                        isDeleted.postValue(false)
                    }
                    HttpConstants.NO_CONTENT_SUCCESS -> {
                        isDeleted.postValue(true)

                    }
                }
            }
        }
        return isDeleted
    }

}