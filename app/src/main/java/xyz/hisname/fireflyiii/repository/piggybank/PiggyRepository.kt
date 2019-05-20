package xyz.hisname.fireflyiii.repository.piggybank

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.PiggyDataDao
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData

@Suppress("RedundantSuspendModifier")
@WorkerThread
class PiggyRepository(private val piggyDao: PiggyDataDao) {

    suspend fun insertPiggy(piggy: PiggyData) = piggyDao.insert(piggy)

    suspend fun retrievePiggyById(piggyId: Long) = piggyDao.getPiggyById(piggyId)

    suspend fun deletePiggyById(piggyId: Long) = piggyDao.deletePiggyById(piggyId)

    suspend fun deleteAllPiggyBank() = piggyDao.deleteAllPiggyBank()

    suspend fun allPiggyBanks() = piggyDao.getAllPiggy()

    suspend fun searchPiggyByName(piggyName: String) = piggyDao.searchPiggyName(piggyName)

    suspend fun getNonCompletedPiggyBanks() = piggyDao.getNonCompletedPiggyBanks()

}