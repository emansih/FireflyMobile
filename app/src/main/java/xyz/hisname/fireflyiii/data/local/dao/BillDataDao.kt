package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.repository.models.bills.BillData

@Dao
abstract class BillDataDao: BaseDao<BillData>{

    @Query("SELECT * FROM bills")
    abstract suspend fun getBill(): List<BillData>

    @Query("SELECT COUNT(*) FROM bills")
    abstract suspend fun getBillCount(): Long

    @Query("DELETE FROM bills WHERE billId = :billId")
    abstract fun deleteBillById(billId: Long): Int

    @Query("SELECT * FROM bills WHERE billId = :billId")
    abstract fun getBillById(billId: Long): BillData

    @Query("DELETE FROM bills")
    abstract suspend fun deleteAllBills(): Int

    @Query("SELECT DISTINCT(name) FROM bills")
    abstract fun getAllBillName(): Flow<List<String>>
}
