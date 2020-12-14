package xyz.hisname.fireflyiii.ui.bills.details

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
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
    private val attachmentRepository = AttachmentRepository(attachmentDao,
            genericService().create(AttachmentService::class.java))

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
        }.flow.cachedIn(viewModelScope).asLiveData()

    fun downloadAttachment(attachmentData: AttachmentData): LiveData<File>{
        isLoading.postValue(true)
        val fileName = attachmentData.attachmentAttributes.filename
        val fileToOpen = File(getApplication<Application>().getExternalFilesDir(null).toString() +
                File.separator + fileName)
        val downloadedFile: MutableLiveData<File> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            val attachmentRepository = AttachmentRepository(attachmentDao,
                    genericService().create(AttachmentService::class.java))
            downloadedFile.postValue(attachmentRepository.downloadOrOpenAttachment(
                    attachmentData.attachmentAttributes.download_uri, fileToOpen))
            isLoading.postValue(false)
        }
        return downloadedFile
    }

}