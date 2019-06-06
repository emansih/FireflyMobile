package xyz.hisname.fireflyiii.repository.piggybank

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.PiggyDataDao
import xyz.hisname.fireflyiii.data.remote.api.PiggybankService
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyModel

@Suppress("RedundantSuspendModifier")
class PiggyRepository(private val piggyDao: PiggyDataDao, private val piggyService: PiggybankService?) {

    suspend fun insertPiggy(piggy: PiggyData) = piggyDao.insert(piggy)

    suspend fun retrievePiggyById(piggyId: Long) = piggyDao.getPiggyById(piggyId)

    suspend fun deletePiggyById(piggyId: Long) = piggyDao.deletePiggyById(piggyId)

    suspend fun allPiggyBanks(): MutableList<PiggyData>{
        var piggyData: MutableList<PiggyData> = arrayListOf()
        var networkCall: Response<PiggyModel>? = null
        try {
            runBlocking(Dispatchers.IO) {
                networkCall = piggyService?.getPaginatedPiggyBank(1)
            }
            piggyData.addAll(networkCall?.body()?.data?.toMutableList() ?: arrayListOf())
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall?.isSuccessful != false) {
                val pagination = responseBody.meta.pagination
                if (pagination.total_pages != pagination.current_page) {
                    runBlocking(Dispatchers.IO) {
                        for (items in 2..pagination.total_pages) {
                            piggyData.addAll(piggyService?.getPaginatedPiggyBank(items)
                                    ?.body()?.data?.toMutableList() ?: arrayListOf())
                        }
                    }
                }
                runBlocking(Dispatchers.IO) {
                    piggyDao.deleteAllPiggyBank()
                }
                piggyData.forEachIndexed { _, piggyBankData ->
                    piggyDao.insert(piggyBankData)
                }
            } else {
                piggyData = piggyDao.getAllPiggy()
            }
        } catch (exception: Exception){
            piggyData = piggyDao.getAllPiggy()
        }
        return piggyData
    }

    suspend fun searchPiggyByName(piggyName: String) = piggyDao.searchPiggyName(piggyName)

    suspend fun getNonCompletedPiggyBanks() = piggyDao.getNonCompletedPiggyBanks()

}