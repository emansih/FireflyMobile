package xyz.hisname.fireflyiii.workers.piggybank

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.remote.api.PiggybankService
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyAttributes
import xyz.hisname.fireflyiii.workers.BaseWorker
import xyz.hisname.fireflyiii.ui.notifications.displayNotification
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class DeletePiggyWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val piggyDataBase by lazy { AppDatabase.getInstance(context).piggyDataDao() }
    private val channelName: String = "Piggy Bank"
    private val channelIcon = R.drawable.ic_sort_descending

    override fun doWork(): Result {
        val piggyId = inputData.getLong("piggyId", 0)
        var piggyAttribute: PiggyAttributes? = null
        GlobalScope.launch(Dispatchers.Main) {
            val result = async(Dispatchers.IO) {
                piggyDataBase.getPiggyById(piggyId)
            }.await()
            piggyAttribute = result[0].piggyAttributes
        }
        genericService?.create(PiggybankService::class.java)?.deletePiggyBankById(piggyId)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                GlobalScope.launch(Dispatchers.Main) {
                    async(Dispatchers.IO) {
                        piggyDataBase.deletePiggyById(piggyId)
                    }.await()
                }
                context.displayNotification(piggyAttribute?.name + "successfully deleted", channelName,
                        Constants.PIGGY_BANK_CHANNEL, channelIcon)
            } else {
                Result.failure()
                context.displayNotification("There was an issue deleting ${piggyAttribute?.name}. " +
                        "Please try again later", "Error deleting Piggy Bank",
                        Constants.PIGGY_BANK_CHANNEL, channelIcon)
            }
        })
        { throwable ->
            Result.failure()
            context.displayNotification(throwable.localizedMessage, "Error deleting Piggy Bank",
                    Constants.PIGGY_BANK_CHANNEL, channelIcon)
        })
        return Result.success()
    }
}