package xyz.hisname.fireflyiii.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData


@Dao
abstract class PiggyDataDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addPiggy(vararg piggyInfo: PiggyData)

    @Query("SELECT * FROM piggy")
    abstract fun getPiggy(): LiveData<MutableList<PiggyData>>

    @Query("DELETE FROM piggy WHERE piggyId = :piggyId")
    abstract fun deletePiggyById(piggyId: Long): Int


}