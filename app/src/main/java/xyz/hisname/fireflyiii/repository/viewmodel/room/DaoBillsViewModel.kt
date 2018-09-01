package xyz.hisname.fireflyiii.repository.viewmodel.room

import android.app.Application
import androidx.lifecycle.*
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.bills.BillData

class DaoBillsViewModel: AndroidViewModel {

    private lateinit var billList: LiveData<MutableList<BillData>>

    constructor(application: Application) : super(application)


    fun getAllBills(): LiveData<MutableList<BillData>>{
        billList = AppDatabase.getInstance(getApplication())?.billDataDao()?.getAllBill()!!
        return billList
    }

    fun deleteBill(data: Long): Int{
        return AppDatabase.getInstance(getApplication())?.billDataDao()?.deleteBillById(data)!!
    }
}