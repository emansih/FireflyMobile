package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.bills.BillPaidDates

@Dao
abstract class BillPaidDao: BaseDao<BillPaidDates> {

    @Query("DELETE FROM billPaidList")
    abstract suspend fun deleteAllPaidList(): Int

    @Query("SELECT * FROM billPaidList WHERE billPaidId =:billId AND date =:date")
    abstract suspend fun getTransactionFromBillId(billId: Long, date: String): List<BillPaidDates>
}