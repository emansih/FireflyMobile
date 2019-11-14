package xyz.hisname.fireflyiii.workers.piggybank

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.remote.firefly.api.PiggybankService
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyAttributes
import xyz.hisname.fireflyiii.repository.piggybank.PiggyRepository
import xyz.hisname.fireflyiii.workers.BaseWorker
import xyz.hisname.fireflyiii.ui.notifications.displayNotification

class DeletePiggyWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val piggyDataBase by lazy { AppDatabase.getInstance(context).piggyDataDao() }
    private val channelName: String = "Piggy Bank"
    private val channelIcon = R.drawable.ic_sort_descending

    companion object {
        fun initWorker(piggyId: Long, context: Context) {
            val accountTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("delete_piggy_$piggyId").get()
            if (accountTag == null || accountTag.size == 0) {
                val accountData = Data.Builder()
                        .putLong("piggyId", piggyId)
                        .build()
                val deleteAccountWork = OneTimeWorkRequest.Builder(DeletePiggyWorker::class.java)
                        .setInputData(accountData)
                        .addTag("delete_piggy_$piggyId")
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED).build())
                        .build()
                WorkManager.getInstance(context).enqueue(deleteAccountWork)
            }
        }
    }

    override suspend fun doWork(): Result {
        val piggyId = inputData.getLong("piggyId", 0)
        var piggyAttribute: PiggyAttributes? = null
        var isDeleted = false
        val repository = PiggyRepository(piggyDataBase, genericService?.create(PiggybankService::class.java))
        runBlocking {
            piggyAttribute = repository.retrievePiggyById(piggyId)[0].piggyAttributes
            isDeleted = repository.deletePiggyById(piggyId, false, applicationContext)
        }
        if (isDeleted) {
            context.displayNotification(piggyAttribute?.name + "successfully deleted", channelName,
                    Constants.PIGGY_BANK_CHANNEL, channelIcon)
        } else {
            Result.failure()
            context.displayNotification("There was an issue deleting ${piggyAttribute?.name}. " +
                    "Please try again later", "Error deleting Piggy Bank",
                    Constants.PIGGY_BANK_CHANNEL, channelIcon)
        }
        return Result.success()
    }
}