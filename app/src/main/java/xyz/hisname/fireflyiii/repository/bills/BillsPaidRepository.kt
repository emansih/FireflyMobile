package xyz.hisname.fireflyiii.repository.bills

import xyz.hisname.fireflyiii.data.local.dao.BillPaidDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.models.bills.BillPaidDates
import java.lang.Exception

class BillsPaidRepository(private val billsPaidDao: BillPaidDao,
                          private val billsService: BillsService?) {

    suspend fun getBillPaidById(billId: Long, startDate: String, endDate: String): List<BillPaidDates>{
        try {
            val networkCall = billsService?.getBillById(billId, startDate, endDate)
            val responseBody = networkCall?.body()
            if(responseBody != null && networkCall.isSuccessful){
                billsPaidDao.deleteByBillId(billId)
                responseBody.data.billAttributes.paid_dates.forEach {  billPaid ->
                    billsPaidDao.insert(BillPaidDates(
                            id = billId, transaction_group_id = billPaid.transaction_group_id,
                            transaction_journal_id = billPaid.transaction_journal_id,
                            date = billPaid.date
                    ))
                }
            }
        } catch (exception: Exception){ }
        return billsPaidDao.getBillsPaidFromIdAndDate(billId, startDate, endDate)
    }
}