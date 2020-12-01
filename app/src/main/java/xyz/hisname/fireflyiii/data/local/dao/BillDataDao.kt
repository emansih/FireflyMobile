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
    abstract suspend fun deleteAllBills(): Int

    //@Query("SELECT * FROM bills WHERE  order by billId")
    //abstract suspend fun getBillByDate(startDate:String, endDate: String): List<BillData>


    @Query("SELECT * FROM bills order by billId desc")
    abstract suspend fun getBills(): MutableList<BillData>

    @Query("SELECT COUNT(*) FROM bills order by billId desc")
    abstract suspend fun getBillsCount(): Long

    @Query("SELECT * FROM bills WHERE name = :billName")
    abstract fun getBillByName(billName: String): MutableList<BillData>
}
