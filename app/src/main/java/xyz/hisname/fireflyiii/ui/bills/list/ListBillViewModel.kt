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
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.bill.DeleteBillWorker

class ListBillViewModel(application: Application): BaseViewModel(application) {

    private val billService = genericService()?.create(BillsService::class.java)
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
            var isItDeleted = 0
            val billList = billRepository.retrieveBillById(billId.toLong())
            if (billList.isNotEmpty()) {
                isItDeleted = billRepository.deleteBillById(billId.toLong())
            }
            // Since onDraw() is being called multiple times, we check if the bill exists locally in the DB.
            when (isItDeleted) {
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
        return isDeleted
    }

}