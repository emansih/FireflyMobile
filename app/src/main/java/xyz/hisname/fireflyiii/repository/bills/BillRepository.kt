package xyz.hisname.fireflyiii.repository.bills

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.BillDataDao
import xyz.hisname.fireflyiii.repository.models.bills.BillData

class BillRepository(private val billDao: BillDataDao) {

    val allBills = billDao.getAllBill()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertBill(bill: BillData){
        billDao.insert(bill)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun retrieveBillById(billId: Long): MutableList<BillData>{
        return billDao.getBillById(billId)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteBillById(billId: Long): Int{
        return billDao.deleteBillById(billId)
    }

}