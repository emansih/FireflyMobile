package xyz.hisname.fireflyiii.repository.bills

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.BillDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.bills.BillsModel
import xyz.hisname.fireflyiii.workers.bill.DeleteBillWorker

@Suppress("RedundantSuspendModifier")
class BillRepository(private val billDao: BillDataDao,
                     private val billService: BillsService?) {

    suspend fun allBills(): MutableList<BillData>{
        var networkCall: Response<BillsModel>? = null
        val billData: MutableList<BillData> = arrayListOf()
        try {
            withContext(Dispatchers.IO) {
                withContext(Dispatchers.IO) {
                    networkCall = billService?.getPaginatedBills(1)
                }
                billData.addAll(networkCall?.body()?.data?.toMutableList() ?: arrayListOf())
            }
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall?.isSuccessful != false) {
                val pagination = responseBody.meta.pagination
                if (pagination.total_pages != pagination.current_page) {
                    withContext(Dispatchers.IO) {
                        for (items in 2..pagination.total_pages) {
                            billData.addAll(
                                    billService?.getPaginatedBills(items)
                                                ?.body()?.data?.toMutableList() ?: arrayListOf()
                            )
                        }
                    }
                }
                withContext(Dispatchers.IO) {
                    billDao.deleteAllBills()
                }
                withContext(Dispatchers.IO) {
                    billData.forEachIndexed { _, data ->
                        insertBill(data)
                    }
                }
            }
        } catch (exception: Exception){ }
        return billDao.getAllBill()
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


    suspend fun deleteBillById(billId: Long, shouldUserWorker: Boolean = false): Boolean{
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
                DeleteBillWorker.initWorker(billId)
            }
            false
        }
    }
}