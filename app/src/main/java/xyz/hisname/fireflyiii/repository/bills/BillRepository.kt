package xyz.hisname.fireflyiii.repository.bills

import android.content.Context
import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.BillDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.workers.bill.DeleteBillWorker

@Suppress("RedundantSuspendModifier")
class BillRepository(private val billDao: BillDataDao,
                     private val billService: BillsService?) {

    suspend fun getPaginatedBills(pageNumber: Int, startDate: String, endDate: String): Flow<MutableList<BillData>> {
        try {
            val networkCall = billService?.getPaginatedBills(pageNumber, startDate, endDate)
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                if (pageNumber == 1) {
                    billDao.deleteAllBills()
                }
                responseBody.data.forEachIndexed { _, billData ->
                    insertBill(billData)
                }
            }
        } catch (exception: Exception){ }
        return billDao.getPaginatedBills(pageNumber * Constants.PAGE_SIZE)
    }

    suspend fun insertBill(bill: BillData) = billDao.insert(bill)


    // Since we are retrieving only 1 item, there is no array. We wrap a try catch to prevent crash ;p
    suspend fun retrieveBillById(billId: Long): MutableList<BillData>{
        try {
            val billData: MutableList<BillData> = arrayListOf()
            val networkCall = billService?.getBillById(billId)
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                billData.addAll(responseBody.data.toMutableList())
                billDao.deleteBillById(billId)
                billData.forEachIndexed { _, data ->
                    insertBill(data)
                }
            }
        } catch (exception: Exception){ }
        return billDao.getBillById(billId)
    }
    
    suspend fun getBillByName(billName: String) = billDao.getBillByName(billName)
    
    suspend fun deleteBillById(billId: Long, shouldUserWorker: Boolean = false, context: Context): Boolean{
        var isDeleted = false
        val networkStatus = billService?.deleteBillById(billId)
        isDeleted = if (networkStatus?.code() == 204 || networkStatus?.code() == 200){
            billDao.deleteBillById(billId)
            true
        } else {
            if(shouldUserWorker){
                DeleteBillWorker.initWorker(billId, context)
            }
            false
        }
        return isDeleted
    }
}