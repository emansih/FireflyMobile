package xyz.hisname.fireflyiii.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData


@Dao
abstract class PiggyDataDao: BaseDao<PiggyData>{

    @Query("SELECT * FROM piggy")
    abstract fun getPiggy(): LiveData<MutableList<PiggyData>>

    @Query("SELECT * FROM piggy")
    abstract fun getAllPiggy(): MutableList<PiggyData>

    @Query("DELETE FROM piggy WHERE piggyId = :piggyId")
    abstract fun deletePiggyById(piggyId: Long): Int

    @Query("SELECT * FROM piggy WHERE piggyId = :piggyId")
    abstract fun getPiggyById(piggyId: Long): MutableList<PiggyData>

}