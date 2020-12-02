package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.bills.BillPayDates


@Dao
abstract class BillPayDao: BaseDao<BillPayDates> {

    @Query("DELETE FROM billPayList")
    abstract suspend fun deleteAllPayList(): Int

    @Query("SELECT * FROM billPayList WHERE id =:billId AND strftime('%s', payDates) BETWEEN strftime('%s', :startDate) AND strftime('%s', :endDate)")
    abstract suspend fun getBillByDateAndId(billId: Long, startDate: String, endDate: String): List<BillPayDates>

}
