package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData


@Dao
abstract class PiggyDataDao: BaseDao<PiggyData>{

    @Query("SELECT * FROM piggy")
    abstract suspend fun getAllPiggy(): List<PiggyData>

    @Query("SELECT COUNT(*) FROM piggy")
    abstract suspend fun getAllPiggyCount(): Int

    @Query("DELETE FROM piggy WHERE piggyId = :piggyId")
    abstract fun deletePiggyById(piggyId: Long): Int

    @Query("DELETE FROM piggy")
    abstract suspend fun deleteAllPiggyBank(): Int

    @Transaction
    @Query("SELECT * FROM piggy JOIN piggyFts ON (piggy.piggyId = piggyFts.piggyId) WHERE piggyFts MATCH :piggyName")
    abstract fun searchPiggyName(piggyName: String): MutableList<PiggyData>

    @Query("SELECT * FROM piggy WHERE piggyId = :piggyId")
    abstract fun getPiggyFromId(piggyId: Long): PiggyData

}