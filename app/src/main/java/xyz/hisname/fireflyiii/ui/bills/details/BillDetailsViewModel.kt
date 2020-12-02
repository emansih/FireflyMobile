package xyz.hisname.fireflyiii.ui.bills.details

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.bills.BillPayRepository
import xyz.hisname.fireflyiii.repository.models.bills.BillPayDates

class BillDetailsViewModel(application: Application): BaseViewModel(application) {

    private val billPayRepository = BillPayRepository(
            AppDatabase.getInstance(application).billPayDao(),
            genericService()?.create(BillsService::class.java))

    fun getPayList(billId: Long, startDate: String, endDate: String): LiveData<List<BillPayDates>>{
        val transactions = MutableLiveData<List<BillPayDates>>()
        viewModelScope.launch(Dispatchers.IO){
            transactions.postValue(billPayRepository.getPaidDatesFromBillId(billId, startDate, endDate))
        }
        return transactions
    }
}