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

package xyz.hisname.fireflyiii.ui.bills.details

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.AttachmentService
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.attachment.AttachmentRepository
import xyz.hisname.fireflyiii.repository.bills.BillPayRepository
import xyz.hisname.fireflyiii.repository.bills.BillRepository
import xyz.hisname.fireflyiii.repository.bills.BillsPaidRepository
import xyz.hisname.fireflyiii.repository.bills.TransactionPagingSource
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.bills.BillPaidDates
import xyz.hisname.fireflyiii.repository.models.bills.BillPayDates
import xyz.hisname.fireflyiii.repository.models.transaction.SplitSeparator
import xyz.hisname.fireflyiii.util.extension.downloadFile
import xyz.hisname.fireflyiii.util.extension.insertDateSeparator
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.bill.DeleteBillWorker
import java.io.File
import java.time.LocalDate

class BillDetailsViewModel(application: Application): BaseViewModel(application) {


    private val billPayDao = AppDatabase.getInstance(application).billPayDao()
    private val billDao = AppDatabase.getInstance(application).billDataDao()
    private val billPaidDao = AppDatabase.getInstance(application).billPaidDao()
    private val transactionDao = AppDatabase.getInstance(application).transactionDataDao()
    private val attachmentDao = AppDatabase.getInstance(getApplication()).attachmentDataDao()
    private val billService = genericService().create(BillsService::class.java)
    private val billPayRepository = BillPayRepository(billPayDao, billService)
    private val billRepository = BillRepository(billDao, billService)
    private val billPaidRepository = BillsPaidRepository(billPaidDao, billService)

    var billId: Long = 0L
    var billName = ""
    val billAttachment = MutableLiveData<List<AttachmentData>>()

    fun getBillInfo(): LiveData<BillData>{
        val billLiveDataList = MutableLiveData<BillData>()
        viewModelScope.launch(Dispatchers.IO){
            val billList = billRepository.getBillById(billId)
            billName =  billList.billAttributes.name
            billLiveDataList.postValue(billList)
            billAttachment.postValue(billRepository.getAttachment(billId, attachmentDao))
        }
        return billLiveDataList
    }

    fun getPayList(startDate: String, endDate: String): LiveData<List<BillPayDates>>{
        val transactions = MutableLiveData<List<BillPayDates>>()
        viewModelScope.launch(Dispatchers.IO){
            transactions.postValue(billPayRepository.getPaidDatesFromBillId(billId, startDate, endDate))
        }
        return transactions
    }

    fun deleteBill(): MutableLiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        isLoading.value = true
        viewModelScope.launch(Dispatchers.IO){
            when (billRepository.deleteBillById(billId)) {
                HttpConstants.FAILED -> {
                    isDeleted.postValue(false)
                    DeleteBillWorker.initPeriodicWorker(billId, getApplication())
                }
                HttpConstants.UNAUTHORISED -> {
                    isDeleted.postValue(false)
                }
                HttpConstants.NO_CONTENT_SUCCESS -> {
                    isDeleted.postValue(true)
                }
            }
            isLoading.postValue(false)
        }
        return isDeleted
    }

    fun getPaidList(startDate: String, endDate: String): LiveData<List<BillPaidDates>>{
        val paidDates = MutableLiveData<List<BillPaidDates>>()
        viewModelScope.launch(Dispatchers.IO) {
            paidDates.postValue(billPaidRepository.getBillPaidById(billId, startDate, endDate))
        }
        return paidDates
    }

    fun getPaidTransactions(date: LocalDate) =
        Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)){
            TransactionPagingSource(billService, transactionDao, billId, date.toString())
        }.flow.insertDateSeparator().cachedIn(viewModelScope).asLiveData()

    fun downloadAttachment(attachmentData: AttachmentData): LiveData<File>{
        val downloadedFile: MutableLiveData<File> = MutableLiveData()
        val fileName = attachmentData.attachmentAttributes.filename
        val fileToOpen = File(getApplication<Application>().getExternalFilesDir(null).toString() +
                File.separator + fileName)
        getApplication<Application>().downloadFile(accManager.accessToken, attachmentData, fileToOpen)
        getApplication<Application>().registerReceiver(object : BroadcastReceiver(){
            override fun onReceive(context: Context, intent: Intent) {
                downloadedFile.postValue(fileToOpen)
            }
        }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        return downloadedFile
    }

}