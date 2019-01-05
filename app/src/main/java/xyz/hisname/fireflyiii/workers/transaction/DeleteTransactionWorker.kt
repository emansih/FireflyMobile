package xyz.hisname.fireflyiii.workers.transaction

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionAttributes
import xyz.hisname.fireflyiii.ui.notifications.displayNotification
import xyz.hisname.fireflyiii.workers.BaseWorker

class DeleteTransactionWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val channelName = "Transactions"
    private val channelIcon = R.drawable.ic_refresh
    private val transactionDatabase by lazy { AppDatabase.getInstance(context).transactionDataDao() }


    override fun doWork(): Result {
        val transactionId = inputData.getLong("transactionId", 0)
        var transactionAttributes: TransactionAttributes? = null

        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
            transactionDatabase.getTransactionById(transactionId)
        }
            transactionAttributes = result[0].transactionAttributes
        }
        val service = genericService?.create(TransactionService::class.java)?.deleteTransactionById(transactionId)
        val responseCode = service?.execute()?.code()
        if(responseCode == 204 or 200){
            GlobalScope.launch {
                withContext(Dispatchers.IO){
                    transactionDatabase.deleteTransactionById(transactionId)
                }
            }
            context.displayNotification(transactionAttributes?.description + " successfully deleted", channelName,
                    Constants.TRANSACTION_CHANNEL, channelName, Constants.TRANSACTION_CHANNEL_DESCRIPTION, channelIcon)
            Result.success()
        } else {
            context.displayNotification("There was issue deleting ${transactionAttributes?.description}",
                    "Failed to delete transaction",
                Constants.TRANSACTION_CHANNEL, channelName, Constants.TRANSACTION_CHANNEL_DESCRIPTION, channelIcon)
            Result.failure()
        }
        return Result.success()
    }

    companion object {
        fun setupWorker(data: Data.Builder, transactionId: Long){
            val transactionWork = OneTimeWorkRequest.Builder(DeleteTransactionWorker::class.java)
                    .setInputData(data.putLong("transactionId" ,transactionId).build())
                    .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build()
            WorkManager.getInstance().enqueue(transactionWork)
        }
    }

}