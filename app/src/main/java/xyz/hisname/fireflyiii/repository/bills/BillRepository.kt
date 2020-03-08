package xyz.hisname.fireflyiii.repository.bills

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.BillDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.bills.BillsModel
import xyz.hisname.fireflyiii.workers.bill.DeleteBillWorker

@Suppress("RedundantSuspendModifier")
class BillRepository(private val billDao: BillDataDao,
                     private val billService: BillsService?) {

    suspend fun getPaginatedBills(pageNumber: Int, startDate: String, endDate: String): MutableList<BillData>{
        var networkCall: Response<BillsModel>? = null
        try {
            withContext(Dispatchers.IO) {
                networkCall = billService?.getPaginatedBills(pageNumber, startDate, endDate)
            }
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall?.isSuccessful == true) {
                if(pageNumber == 1){
                    withContext(Dispatchers.IO) {
                        billDao.deleteAllBills()
                    }
                }
                withContext(Dispatchers.IO) {
                    responseBody.data.forEachIndexed { _, billData ->
                        insertBill(billData)
                    }
                }

            }
        } catch (exception: Exception){ }
        return billDao.getPaginatedBills(pageNumber * Constants.PAGE_SIZE)
    }

    suspend fun insertBill(bill: BillData) = billDao.insert(bill)


    suspend fun retrieveBillById(billId: Long): MutableList<BillData>{
        var networkCall: Response<BillsModel>? = null
        val billData: MutableList<BillData> = arrayListOf()
        try {
            withContext(Dispatchers.IO) {
                withContext(Dispatchers.IO){
                    networkCall = billService?.getBillById(billId)
                }
                billData.addAll(networkCall?.body()?.data?.toMutableList() ?: arrayListOf())
            }
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall?.isSuccessful != false) {
                withContext(Dispatchers.IO) {
                    billDao.deleteBillById(billId)
                }
                withContext(Dispatchers.IO) {
                    billData.forEachIndexed { _, data ->
                        insertBill(data)
                    }
                }
            }
        } catch (exception: Exception){ }
        return billDao.getBillById(billId)
    }


    suspend fun deleteBillById(billId: Long, shouldUserWorker: Boolean = false, context: Context): Boolean{
        var networkStatus: Response<BillsModel>? = null
        withContext(Dispatchers.IO) {
            networkStatus = billService?.deleteBillById(billId)
        }
        return if (networkStatus?.code() == 204 || networkStatus?.code() == 200){
            withContext(Dispatchers.IO) {
                billDao.deleteBillById(billId)
            }
            true
        } else {
            if(shouldUserWorker){
                DeleteBillWorker.initWorker(billId, context)
            }
            false
        }
    }
}