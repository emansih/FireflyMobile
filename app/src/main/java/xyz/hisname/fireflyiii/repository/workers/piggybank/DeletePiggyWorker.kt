package xyz.hisname.fireflyiii.repository.workers.piggybank

import android.content.Context
import androidx.work.WorkerParameters
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.api.PiggybankService
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyAttributes
import xyz.hisname.fireflyiii.repository.workers.BaseWorker
import xyz.hisname.fireflyiii.ui.notifications.displayNotification
import xyz.hisname.fireflyiii.util.retrofitCallback

class DeletePiggyWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val piggyDataBase by lazy { AppDatabase.getInstance(context)?.piggyDataDao() }
    private val channelName: String = "Piggy Bank"
    private val channelDescription = "Show Piggy Bank Notifications"
    private val channelIcon = R.drawable.ic_sort_descending

    override fun doWork(): Result {
        val id = inputData.getString("piggyId") ?: ""
        var piggyAttribute: PiggyAttributes? = null
        GlobalScope.launch(Dispatchers.Main) {
            val result = async(Dispatchers.IO) {
                piggyDataBase?.getPiggyById(id.toLong())
            }.await()
            piggyAttribute = result!![0].piggyAttributes
        }
        genericService?.create(PiggybankService::class.java)?.deletePiggyBankById(id)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                GlobalScope.launch(Dispatchers.Main) {
                    async(Dispatchers.IO) {
                        piggyDataBase?.deletePiggyById(id.toLong())
                    }.await()
                }
                context.displayNotification(piggyAttribute?.name + "successfully deleted", "Piggy Bank",
                        Constants.PIGGY_BANK_CHANNEL, channelName, channelDescription, channelIcon)
            } else {
                context.displayNotification("There was an issue deleting ${piggyAttribute?.name}. " +
                        "Please try again later", "Error deleting Piggy Bank",
                        Constants.PIGGY_BANK_CHANNEL, channelName, channelDescription, channelIcon)
            }
        })
        { throwable ->
            context.displayNotification(throwable.localizedMessage, "Error deleting Piggy Bank",
                    Constants.PIGGY_BANK_CHANNEL, channelName, channelDescription, channelIcon)
        })
        return Result.SUCCESS
    }
}