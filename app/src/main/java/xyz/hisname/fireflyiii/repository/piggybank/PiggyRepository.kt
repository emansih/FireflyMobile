package xyz.hisname.fireflyiii.repository.piggybank

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.PiggyDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.PiggybankService
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyModel
import xyz.hisname.fireflyiii.workers.piggybank.DeletePiggyWorker

@Suppress("RedundantSuspendModifier")
class PiggyRepository(private val piggyDao: PiggyDataDao, private val piggyService: PiggybankService?) {

    suspend fun insertPiggy(piggy: PiggyData) = piggyDao.insert(piggy)

    suspend fun retrievePiggyById(piggyId: Long) = piggyDao.getPiggyById(piggyId)

    suspend fun deletePiggyById(piggyId: Long, shouldUseWorker: Boolean = false, context: Context): Boolean {
        var networkResponse: Response<PiggyModel>? = null
        withContext(Dispatchers.IO){
            networkResponse = piggyService?.deletePiggyBankById(piggyId)
        }
        return if (networkResponse?.code() == 204 || networkResponse?.code() == 200){
            runBlocking(Dispatchers.IO) {
                piggyDao.deletePiggyById(piggyId)
            }
            true
        } else {
            if(shouldUseWorker){
                DeletePiggyWorker.initWorker(piggyId, context)
            }
            false
        }
    }

    suspend fun allPiggyBanks(): MutableList<PiggyData>{
        val piggyData: MutableList<PiggyData> = arrayListOf()
        var networkCall: Response<PiggyModel>? = null
        try {
            withContext(Dispatchers.IO) {
                withContext(Dispatchers.IO){
                    networkCall = piggyService?.getPaginatedPiggyBank(1)
                }
                piggyData.addAll(networkCall?.body()?.data?.toMutableList() ?: arrayListOf())
            }
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall?.isSuccessful != false) {
                val pagination = responseBody.meta.pagination
                if (pagination.total_pages != pagination.current_page) {
                    withContext(Dispatchers.IO) {
                        for (items in 2..pagination.total_pages) {
                            piggyData.addAll(piggyService?.getPaginatedPiggyBank(items)
                                    ?.body()?.data?.toMutableList() ?: arrayListOf())
                        }
                    }
                }
                withContext(Dispatchers.IO) {
                    piggyDao.deleteAllPiggyBank()
                }
                piggyData.forEachIndexed { _, piggyBankData ->
                    insertPiggy(piggyBankData)
                }
            }
        } catch (exception: Exception) { }
        return piggyDao.getAllPiggy()
    }

    suspend fun searchPiggyByName(piggyName: String) = piggyDao.searchPiggyName(piggyName)

    suspend fun getNonCompletedPiggyBanks() = piggyDao.getNonCompletedPiggyBanks()

}