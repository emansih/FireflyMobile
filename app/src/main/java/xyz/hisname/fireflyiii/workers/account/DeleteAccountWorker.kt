package xyz.hisname.fireflyiii.workers.account

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.*
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.account.AccountRepository
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.BaseWorker
import java.time.Duration
import java.util.concurrent.TimeUnit

class DeleteAccountWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val accountDatabase by lazy { AppDatabase.getInstance(context).accountDataDao() }

    companion object {
        fun initPeriodicWorker(accountId: Long, context: Context){
            val accountTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("delete_periodic_account_$accountId").get()
            if(accountTag == null || accountTag.size == 0) {
                val accountData = Data.Builder()
                        .putLong("accountId", accountId)
                        .build()
                val delay = AppPref(PreferenceManager.getDefaultSharedPreferences(context)).workManagerDelay
                val deleteAccountWork = PeriodicWorkRequestBuilder<DeleteAccountWorker>(Duration.ofMinutes(delay))
                        .setInputData(accountData)
                        .addTag("delete_periodic_account_$accountId")
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .setRequiresBatteryNotLow(true)
                                .build())
                        .build()
                WorkManager.getInstance(context).enqueue(deleteAccountWork)
            }
        }

        fun cancelWorker(accountId: Long, context: Context){
            WorkManager.getInstance(context).cancelAllWorkByTag("delete_periodic_account_$accountId")
        }
    }

    override suspend fun doWork(): Result {
        val accountId = inputData.getLong("accountId", 0L)
        val accountService = genericService?.create(AccountsService::class.java)
        val repository = AccountRepository(accountDatabase, accountService)
        return when(repository.deleteAccountById(accountId)){
            HttpConstants.NO_CONTENT_SUCCESS -> {
                cancelWorker(accountId, context)
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