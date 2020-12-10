package xyz.hisname.fireflyiii.ui.bills

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.workers.bill.BillWorker

class AddBillViewModel(application: Application): BaseViewModel(application) {

    private val billRepository = BillRepository(
            AppDatabase.getInstance(application).billDataDao(),
            genericService().create(BillsService::class.java)
    )

    private val currencyRepository = CurrencyRepository(
            AppDatabase.getInstance(application).currencyDataDao(),
            genericService().create(CurrencyService::class.java)
    )

    private var currencyCode: String = ""

    val attachmentMessageLiveData = MutableLiveData<ArrayList<String>>()


    fun getBillById(billId: Long): LiveData<BillData>{
        isLoading.postValue(true)
        val billLiveData: MutableLiveData<BillData> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            val billList = billRepository.getBillById(billId)
            currencyCode =  billList.billAttributes?.currency_code ?: ""
            billLiveData.postValue(billList)
            isLoading.postValue(false)
        }
        return billLiveData
    }


    fun addBill(name: String, amountMin: String, amountMax: String, date: String, repeatFreq: String,
                skip: String, active: String, currencyCode: String,notes: String?, fileToUpload: ArrayList<Uri>): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            val addBill = billRepository.addBill(name, amountMin, amountMax, date, repeatFreq, skip,
                    active, currencyCode, notes)
            when {
                addBill.response != null -> {
                    apiResponse.postValue(Pair(true,
                            getApplication<Application>().getString(R.string.stored_new_bill, name)))
                    if(fileToUpload.isNotEmpty()) {
                        val attachmentRepository = AttachmentRepository(AppDatabase.getInstance(getApplication()).attachmentDataDao(),
                                genericService().create(AttachmentService::class.java))
                        val attachmentResponse =
                                attachmentRepository.uploadFile(getApplication(), addBill.response.data.billId,
                                        fileToUpload, AttachableType.BILL)
                        attachmentMessageLiveData.postValue(attachmentResponse)
                    }
                }
                addBill.errorMessage != null -> {
                    apiResponse.postValue(Pair(false,addBill.errorMessage))
                }
                addBill.error != null -> {
                    apiResponse.postValue(Pair(false,
                            getApplication<Application>().getString(R.string.data_added_when_user_online, "Bill")))
                    BillWorker.initWorker(getApplication(), name, amountMin, amountMax, date, repeatFreq,
                            skip, currencyCode, notes)
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