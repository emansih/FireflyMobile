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

package xyz.hisname.fireflyiii.ui.bills

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.AttachmentService
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.attachment.AttachableType
import xyz.hisname.fireflyiii.repository.attachment.AttachmentRepository
import xyz.hisname.fireflyiii.repository.bills.BillRepository
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.workers.AttachmentWorker
import xyz.hisname.fireflyiii.workers.bill.BillWorker
import java.io.File
import java.net.UnknownHostException

class AddBillViewModel(application: Application): BaseViewModel(application) {

    private val billRepository = BillRepository(
            AppDatabase.getInstance(application).billDataDao(),
            genericService().create(BillsService::class.java)
    )

    private val currencyRepository = CurrencyRepository(
            AppDatabase.getInstance(application).currencyDataDao(),
            genericService().create(CurrencyService::class.java)
    )
    private val attachmentDao = AppDatabase.getInstance(getApplication()).attachmentDataDao()
    private val attachmentService = genericService().create(AttachmentService::class.java)
    private val attachmentRepository = AttachmentRepository(attachmentDao, attachmentService)

    private var currencyCode: String = ""
    val billAttachment = MutableLiveData<List<AttachmentData>>()

    fun getBillById(billId: Long): LiveData<BillData>{
        isLoading.postValue(true)
        val billLiveData: MutableLiveData<BillData> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            val billList = billRepository.getBillById(billId)
            currencyCode =  billList.billAttributes.currency_code
            billLiveData.postValue(billList)
            isLoading.postValue(false)
            billAttachment.postValue(billRepository.getAttachment(billId, attachmentDao))
        }
        return billLiveData
    }

    fun deleteAttachment(data: AttachmentData): LiveData<Boolean>{
        val isSuccessful = MutableLiveData<Boolean>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            isLoading.postValue(false)
            val fileName = getApplication<Application>().getExternalFilesDir(null).toString() +
                    File.separator + data.attachmentAttributes.filename
            attachmentRepository.deleteAttachment(data, fileName)
        }
        return isSuccessful
    }

    fun uploadFile(billId: Long, fileToUpload: ArrayList<Uri>): LiveData<List<WorkInfo>> {
        return AttachmentWorker.initWorker(fileToUpload, billId,
                getApplication<Application>(), AttachableType.BILL)
    }

        fun addBill(name: String, amountMin: String, amountMax: String, date: String, repeatFreq: String,
                skip: String, active: String, currencyCode: String,notes: String?, fileToUpload: ArrayList<Uri>): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(CoroutineExceptionHandler { _, _ -> }){
            val addBill = billRepository.addBill(name, amountMin, amountMax, date, repeatFreq, skip,
                    active, currencyCode, notes)
            when {
                addBill.response != null -> {
                    apiResponse.postValue(Pair(true,
                            getApplication<Application>().getString(R.string.stored_new_bill, name)))
                    if(fileToUpload.isNotEmpty()) {
                        uploadFile(addBill.response.data.billId, fileToUpload)
                    }
                }
                addBill.errorMessage != null -> {
                    apiResponse.postValue(Pair(false,addBill.errorMessage))
                }
                addBill.error != null -> {
                    if(addBill.error is UnknownHostException) {
                        apiResponse.postValue(Pair(true,
                                getApplication<Application>().getString(R.string.data_added_when_user_online, "Bill")))
                        BillWorker.initWorker(getApplication(), name, amountMin, amountMax, date, repeatFreq,
                                skip, currencyCode, notes, fileToUpload)
                    } else {
                        apiResponse.postValue(Pair(false, addBill.error.localizedMessage))
                    }
                }
                else -> {
                    apiResponse.postValue(Pair(false, "Error saving bill"))
                }
            }
            isLoading.postValue(false)
        }
        return apiResponse
    }

    fun updateBill(billId: Long, name: String, amountMin: String, amountMax: String, date: String, repeatFreq: String,
                   skip: String, active: String, currencyCode: String,notes: String?): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            val updateBill = billRepository.updateBill(billId,
                    name, amountMin, amountMax, date, repeatFreq, skip, active, currencyCode, notes)
            when {
                updateBill.response != null -> {
                    apiResponse.postValue(Pair(true,
                            getApplication<Application>().getString(R.string.updated_bill, name)))
                }
                updateBill.errorMessage != null -> {
                    apiResponse.postValue(Pair(false, updateBill.errorMessage))
                }
                updateBill.error != null -> {
                    apiResponse.postValue(Pair(false, updateBill.error.localizedMessage))
                }
                else -> {
                    apiResponse.postValue(Pair(false, "Error updating bill"))
                }
            }
            isLoading.postValue(false)
        }
        return apiResponse
    }

    fun getDefaultCurrency(): LiveData<CurrencyData>{
        val currencyLiveData = MutableLiveData<CurrencyData>()
        viewModelScope.launch(Dispatchers.IO){
            currencyLiveData.postValue(currencyRepository.defaultCurrency())
        }
        return currencyLiveData
    }

    fun getBillCurrencyDetails(billId: Long): LiveData<String>{
        val currencyLiveData = MutableLiveData<String>()
        viewModelScope.launch(Dispatchers.IO){
            val currencyName = currencyRepository.getCurrencyFromBillId(billId, currencyCode)
            if(currencyName.isEmpty()){
                // User is offline and does not have the currency stored locally
                currencyLiveData.postValue(currencyCode)
                apiResponse.postValue("Some data could not be loaded as you are offline")
            } else {
                currencyLiveData.postValue("$currencyName ($currencyCode)")
            }
        }
        return currencyLiveData
    }
}