package xyz.hisname.fireflyiii.repository.bills

import android.app.Application
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.bills.BillData

open class BaseBillViewModel(application: Application): BaseViewModel(application)  {

    val repository: BillRepository
    var billData: MutableList<BillData>? = null

    init {
        val billDataDao = AppDatabase.getInstance(application).billDataDao()
        repository = BillRepository(billDataDao)
    }
}