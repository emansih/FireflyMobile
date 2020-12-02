package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.bills.BillPayDates


@Dao
abstract class BillPayDao: BaseDao<BillPayDates> {

    @Query("DELETE FROM billPayList")
    abstract suspend fun deleteAllPayList(): Int

    @Query("SELECT * FROM billPayList INNER JOIN bills ON bills.billId = billPayList.billId WHERE billPayList.payDates =:billDate")
    abstract suspend fun getBillsByDate(billDate: String): MutableList<BillData>

    @Query("SELECT COUNT(*) FROM billPayList INNER JOIN bills ON bills.billId = billPayList.billId WHERE billPayList.payDates =:billDate")
    abstract suspend fun getBillsByDateCount(billDate: String): Long

}