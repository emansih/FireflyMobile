package xyz.hisname.fireflyiii.repository.piggybank

import android.content.Context
import xyz.hisname.fireflyiii.data.local.dao.PiggyDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.PiggybankService
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.workers.piggybank.DeletePiggyWorker

@Suppress("RedundantSuspendModifier")
class PiggyRepository(private val piggyDao: PiggyDataDao, private val piggyService: PiggybankService?) {

    suspend fun insertPiggy(piggy: PiggyData) = piggyDao.insert(piggy)

    suspend fun retrievePiggyById(piggyId: Long) = piggyDao.getPiggyById(piggyId)

    suspend fun deletePiggyById(piggyId: Long, shouldUseWorker: Boolean = false, context: Context): Boolean {
        var isDeleted = false
        try {
            val networkResponse = piggyService?.deletePiggyBankById(piggyId)
            isDeleted = if (networkResponse?.code() == 204 || networkResponse?.code() == 200) {
                piggyDao.deletePiggyById(piggyId)
                true
            } else {
                if (shouldUseWorker) {
                    DeletePiggyWorker.initWorker(piggyId, context)
                }
                false
            }
        } catch (exception: Exception){ }
        return isDeleted
    }

    suspend fun allPiggyBanks(): MutableList<PiggyData>{
        val piggyData: MutableList<PiggyData> = arrayListOf()
        try {
            val networkCall = piggyService?.getPaginatedPiggyBank(1)
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                piggyData.addAll(responseBody.data.toMutableList())
                val pagination = responseBody.meta.pagination
                if (pagination.total_pages != pagination.current_page) {
                    for (items in 2..pagination.total_pages) {
                        piggyData.addAll(piggyService?.getPaginatedPiggyBank(items)
                                ?.body()?.data?.toMutableList() ?: arrayListOf())
                    }
                }
                piggyDao.deleteAllPiggyBank()
                piggyData.forEachIndexed { _, piggyBankData ->
                    insertPiggy(piggyBankData)
                }
            }
        } catch (exception: Exception){ }
        return piggyDao.getAllPiggy()
    }

    suspend fun searchPiggyByName(piggyName: String) = piggyDao.searchPiggyName(piggyName)

    suspend fun getNonCompletedPiggyBanks() = piggyDao.getNonCompletedPiggyBanks()

}