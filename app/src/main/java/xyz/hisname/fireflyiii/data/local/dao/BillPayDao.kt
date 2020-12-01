package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.bills.BillPayDates


@Dao
abstract class BillPayDao: BaseDao<BillPayDates> {

    @Query("DELETE FROM billPayList")
    abstract suspend fun deleteAllPayList(): Int
}