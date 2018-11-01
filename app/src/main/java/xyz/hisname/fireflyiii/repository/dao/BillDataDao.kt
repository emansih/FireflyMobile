package xyz.hisname.fireflyiii.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import xyz.hisname.fireflyiii.repository.models.bills.BillData

@Dao
abstract class BillDataDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addBill(vararg billInfo: BillData)

    @Query("SELECT * FROM bills")
    abstract fun getAllBill(): LiveData<MutableList<BillData>>

    @Query("DELETE FROM bills WHERE billId = :billId")
    abstract fun deleteBillById(billId: Long): Int

    @Query("SELECT * FROM bills WHERE billId = :billId")
    abstract fun getBillById(billId: Long): MutableList<BillData>
}
