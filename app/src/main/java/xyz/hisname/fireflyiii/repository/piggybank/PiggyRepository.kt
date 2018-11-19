package xyz.hisname.fireflyiii.repository.piggybank

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.PiggyDataDao
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData

class PiggyRepository(private val piggyDao: PiggyDataDao) {

    val allPiggyBanks = piggyDao.getAllPiggy()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertPiggy(piggy: PiggyData){
        piggyDao.insert(piggy)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun retrievePiggyById(piggyId: Long): MutableList<PiggyData>{
        return piggyDao.getPiggyById(piggyId)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deletePiggyById(piggyId: Long): Int{
        return piggyDao.deletePiggyById(piggyId)
    }

}