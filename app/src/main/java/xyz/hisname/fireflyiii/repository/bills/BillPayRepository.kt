package xyz.hisname.fireflyiii.repository.bills

import xyz.hisname.fireflyiii.data.local.dao.BillPayDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.models.bills.BillPayDates
import java.lang.Exception
import java.time.LocalDate

class BillPayRepository(private val billPayDao: BillPayDao,
                        private val billsService: BillsService) {


    suspend fun getPaidDatesFromBillId(billId: Long, startDate: String, endDate: String): List<BillPayDates>{
        try {
            val networkCall = billsService.getBillById(billId, startDate, endDate)
            val responseBody = networkCall.body()
            if(responseBody != null && networkCall.isSuccessful){
                responseBody.data.billAttributes?.pay_dates?.forEach { localDate ->
                    billPayDao.insert(BillPayDates(id = billId, payDates = LocalDate.parse(localDate)))
                }
            }
        } catch (exception: Exception){ }
        return billPayDao.getBillByDateAndId(billId,startDate, endDate)
    }

}