package xyz.hisname.fireflyiii.workers.piggybank

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.*
import xyz.hisname.fireflyiii.data.remote.firefly.api.PiggybankService
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.piggybank.PiggyRepository
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.BaseWorker
import java.time.Duration
import java.util.concurrent.TimeUnit

class DeletePiggyWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val piggyDataBase by lazy { AppDatabase.getInstance(context).piggyDataDao() }

    companion object {
        fun initPeriodicWorker(piggyId: Long, context: Context){
            val piggyTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("delete_periodic_piggy_$piggyId").get()
            if (piggyTag == null || piggyTag.size == 0) {
                val piggyData = Data.Builder()
                        .putLong("piggyId", piggyId)
                        .build()
                val delay = AppPref(PreferenceManager.getDefaultSharedPreferences(context)).workManagerDelay
                val deletePiggyWork = PeriodicWorkRequestBuilder<DeletePiggyWorker>(Duration.ofMinutes(delay))
                        .setInputData(piggyData)
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .setRequiresBatteryNotLow(true)
                                .build())
                        .addTag("delete_periodic_piggy_$piggyId")
                        .build()
                WorkManager.getInstance(context).enqueue(deletePiggyWork)
            }
        }

        fun cancelWorker(piggyId: Long, context: Context){
            WorkManager.getInstance(context).cancelAllWorkByTag("delete_periodic_piggy_$piggyId")
        }
    }

    override suspend fun doWork(): Result {
        val piggyId = inputData.getLong("piggyId", 0)
        val repository = PiggyRepository(piggyDataBase, genericService?.create(PiggybankService::class.java))
        return when (repository.deletePiggyById(piggyId)) {
            HttpConstants.NO_CONTENT_SUCCESS -> {
                cancelWorker(piggyId, context)
                Result.success()
            }
            HttpConstants.FAILED -> {
                Result.retry()
            }
            else -> {
                Result.failure()
            }
        }
    }
}