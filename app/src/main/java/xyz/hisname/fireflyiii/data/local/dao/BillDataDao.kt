package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.repository.models.bills.BillData

@Dao
abstract class BillDataDao: BaseDao<BillData>{

    @Query("SELECT * FROM bills")
    abstract fun getAllBill(): MutableList<BillData>

    @Query("DELETE FROM bills WHERE billId = :billId")
    abstract fun deleteBillById(billId: Long): Int

    @Query("SELECT * FROM bills WHERE billId = :billId")
    abstract fun getBillById(billId: Long): MutableList<BillData>

    @Query("DELETE FROM bills")
    abstract fun deleteAllBills(): Int

    @Query("SELECT * FROM bills order by billId desc limit :limitNumber")
    abstract fun getPaginatedBills(limitNumber: Int): Flow<MutableList<BillData>>

    @Query("SELECT * FROM bills WHERE name = :billName")
    abstract fun getBillByName(billName: String): MutableList<BillData>
}
