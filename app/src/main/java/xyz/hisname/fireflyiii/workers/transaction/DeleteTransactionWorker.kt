package xyz.hisname.fireflyiii.workers.transaction

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.*
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.BaseWorker
import java.time.Duration
import java.util.concurrent.TimeUnit

class DeleteTransactionWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val transactionDatabase by lazy { AppDatabase.getInstance(context).transactionDataDao() }

    override suspend fun doWork(): Result {
        val transactionId = inputData.getLong("transactionId", 0)
        val repository = TransactionRepository(transactionDatabase, genericService?.create(TransactionService::class.java))
        return when (repository.deleteTransactionById(transactionId)) {
            HttpConstants.NO_CONTENT_SUCCESS -> {
                cancelWorker(transactionId, context)
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


    companion object {
        fun setupWorker(transactionId: Long, context: Context){
            val transactionTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("delete_periodic_transaction_$transactionId").get()
            if (transactionTag == null || transactionTag.size == 0) {
                val transactionData = Data.Builder()
                        .putLong("transactionId", transactionId)
                        .build()
                val delay = AppPref(PreferenceManager.getDefaultSharedPreferences(context)).workManagerDelay
                val transactionWork = PeriodicWorkRequestBuilder<DeleteTransactionWorker>(Duration.ofMinutes(delay))
                        .setInputData(transactionData)
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .setRequiresBatteryNotLow(true)
                                .build())
                        .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
                        .build()
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                        "delete_periodic_transaction_$transactionId",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        transactionWork)
            }

        }

        fun cancelWorker(transactionId: Long, context: Context){
            WorkManager.getInstance(context).cancelAllWorkByTag("delete_periodic_transaction_$transactionId")
        }
    }

}