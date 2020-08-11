package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData


@Dao
abstract class PiggyDataDao: BaseDao<PiggyData>{

    @Query("SELECT * FROM piggy")
    abstract fun getAllPiggy(): Flow<MutableList<PiggyData>>

    @Query("DELETE FROM piggy WHERE piggyId = :piggyId")
    abstract fun deletePiggyById(piggyId: Long): Int

    @Query("SELECT * FROM piggy WHERE piggyId = :piggyId")
    abstract fun getPiggyById(piggyId: Long): MutableList<PiggyData>

    @Query("DELETE FROM piggy")
    abstract fun deleteAllPiggyBank(): Int

    @Transaction
    @Query("SELECT piggy.name FROM piggy JOIN piggyFts ON (piggy.piggyId = piggyFts.piggyId) WHERE piggyFts MATCH :piggyName")
    abstract fun searchPiggyName(piggyName: String): MutableList<PiggyData>

    @Query("SELECT * FROM piggy WHERE percentage != 100")
    abstract fun getNonCompletedPiggyBanks(): Flow<MutableList<PiggyData>>

    @Query("SELECT piggyId FROM piggy WHERE name = :piggyName")
    abstract fun getPiggyIdFromName(piggyName: String): Long

}