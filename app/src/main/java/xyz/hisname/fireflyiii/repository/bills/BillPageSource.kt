package xyz.hisname.fireflyiii.repository.bills

import androidx.paging.PagingSource
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.BillDataDao
import xyz.hisname.fireflyiii.data.local.dao.BillPaidDao
import xyz.hisname.fireflyiii.data.local.dao.BillPayDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.bills.BillPaidDates
import xyz.hisname.fireflyiii.repository.models.bills.BillPayDates
import java.time.LocalDate


class BillPageSource(private val billsService: BillsService?,
                     private val billDao: BillDataDao,
                     private val billPaidDao: BillPaidDao,
                     private val billPayDao: BillPayDao,
                     private val startDate: String,
                     private val endDate: String): PagingSource<Int, BillData>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BillData> {
        val paramKey = params.key
        val previousKey = if(paramKey != null){
            if(paramKey - 1 == 0){
                null
            } else {
                paramKey - 1
            }
        } else {
            null
        }
        try {
            val networkCall = billsService?.getPaginatedBills(params.key ?: 1, startDate, endDate)
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                if (params.key == null) {
                    billDao.deleteAllBills()
                    billPaidDao.deleteAllPaidList()
                }
                responseBody.data.forEach { data ->
                    billDao.insert(data)
                    data.billAttributes?.paid_dates?.forEach {  paid ->
                        billPaidDao.insert(BillPaidDates(
                                data.billId ?: 0,
                                paid.transaction_group_id,
                                paid.transaction_journal_id,
                                paid.date
                        ))
                    }
                    data.billAttributes?.pay_dates?.forEach {  pay ->
                        billPayDao.insert(BillPayDates(
                                data.billId ?: 0, LocalDate.parse(pay)
                        ))
                    }
                }
            }
            val pagination = responseBody?.meta?.pagination
            if(pagination != null){
                val nextKey = if(pagination.current_page < pagination.total_pages){
                    pagination.current_page + 1
                } else {
                    null
                }
                return LoadResult.Page(billDao.getBills(), previousKey, nextKey)
            } else {
                return getOfflineData(params.key, previousKey)
            }
        } catch (exception: Exception){
            return getOfflineData(params.key, previousKey)
        }
    }

    private suspend fun getOfflineData(paramKey: Int?, previousKey: Int?): LoadResult<Int, BillData>{
        val numberOfRows = billDao.getBillsCount()
        val nextKey = if(paramKey ?: 1 < (numberOfRows / Constants.PAGE_SIZE)){
            paramKey ?: 1 + 1
        } else {
            null
        }
        return LoadResult.Page(billDao.getBills(), previousKey, nextKey)

    }

    override val keyReuseSupported = true

}
