package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.bills.BillPaidDates

@Dao
abstract class BillPaidDao: BaseDao<BillPaidDates> {

    @Query("DELETE FROM billPaidList")
    abstract suspend fun deleteAllPaidList(): Int

    @Query("DELETE FROM billPaidList WHERE id =:billId")
    abstract suspend fun deleteByBillId(billId: Long): Int

    @Query("SELECT * FROM billPaidList WHERE id =:billId  AND strftime('%s', date) BETWEEN strftime('%s', :startDate) AND strftime('%s', :endDate)")
    abstract suspend fun getBillsPaidFromIdAndDate(billId: Long, startDate: String, endDate: String): List<BillPaidDates>
}