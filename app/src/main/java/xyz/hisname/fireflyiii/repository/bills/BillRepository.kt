package xyz.hisname.fireflyiii.repository.bills

import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.BillDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.util.network.HttpConstants
import java.net.UnknownHostException

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
    
    suspend fun deleteBillById(billId: Long): Int{
        try {
            val networkResponse = billService?.deleteBillById(billId)
            when (networkResponse?.code()) {
                204 -> {
                    billDao.deleteBillById(billId)
                    return HttpConstants.NO_CONTENT_SUCCESS
                }
                401 -> {
                    /*   User is unauthenticated. We will retain user's data as we are
                     *   now in inconsistent state. This use case is unlikely to happen unless user
                     *   deletes their token from the web interface without updating the mobile client
                     */
                    return HttpConstants.UNAUTHORISED
                }
                404 -> {
                    // User probably deleted this on the web interface and tried to do it using mobile client
                    billDao.deleteBillById(billId)
                    return HttpConstants.NOT_FOUND
                }
                else -> {
                    return HttpConstants.FAILED
                }
            }
        } catch (exception: UnknownHostException){
            billDao.deleteBillById(billId)
            return HttpConstants.FAILED
        }
    }
}