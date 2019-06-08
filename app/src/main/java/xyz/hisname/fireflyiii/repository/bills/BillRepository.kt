package xyz.hisname.fireflyiii.repository.bills

import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.BillDataDao
import xyz.hisname.fireflyiii.data.remote.api.BillsService
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
            runBlocking(Dispatchers.IO) {
                networkCall = billService?.getPaginatedBills(1)
                billData.addAll(networkCall?.body()?.data?.toMutableList() ?: arrayListOf())
            }
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall?.isSuccessful != false) {
                val pagination = responseBody.meta.pagination
                if (pagination.total_pages != pagination.current_page) {
                    runBlocking(Dispatchers.IO) {
                        for (items in 2..pagination.total_pages) {
                            billData.addAll(
                                    billService?.getPaginatedBills(items)
                                                ?.body()?.data?.toMutableList() ?: arrayListOf()
                            )
                        }
                    }
                }
                runBlocking(Dispatchers.IO) {
                    billDao.deleteAllBills()
                }
                runBlocking(Dispatchers.IO) {
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
            runBlocking(Dispatchers.IO) {
                networkCall = billService?.getBillById(billId)
                billData.addAll(networkCall?.body()?.data?.toMutableList() ?: arrayListOf())
            }
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall?.isSuccessful != false) {
                runBlocking(Dispatchers.IO) {
                    billDao.deleteBillById(billId)
                }
                runBlocking(Dispatchers.IO) {
                    billData.forEachIndexed { _, data ->
                        insertBill(data)
                    }
                }
            }
        } catch (exception: Exception){ }
        return billDao.getBillById(billId)
    }


    suspend fun deleteBillById(billId: Long): Boolean{
        var networkStatus: Response<BillsModel>? = null
        runBlocking {
            networkStatus = billService?.deleteBillById(billId)
        }
        return if (networkStatus?.code() == 204 || networkStatus?.code() == 200){
            runBlocking(Dispatchers.IO) {
                billDao.deleteBillById(billId)
            }
            true
        } else {
            deleteBillWorker(billId)
            false
        }
    }


    private fun deleteBillWorker(billId: Long){
        val accountTag =
                WorkManager.getInstance().getWorkInfosByTag("delete_bill_$billId").get()
        if(accountTag == null || accountTag.size == 0) {
            val accountData = Data.Builder()
                    .putLong("billId", billId)
                    .build()
            val deleteAccountWork = OneTimeWorkRequest.Builder(DeleteBillWorker::class.java)
                    .setInputData(accountData)
                    .addTag("delete_bill_$billId")
                    .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build()
            WorkManager.getInstance().enqueue(deleteAccountWork)
        }
    }
}