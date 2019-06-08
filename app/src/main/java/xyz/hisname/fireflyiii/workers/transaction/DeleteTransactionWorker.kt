package xyz.hisname.fireflyiii.workers.transaction

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionAttributes
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.ui.notifications.displayNotification
import xyz.hisname.fireflyiii.workers.BaseWorker

class DeleteTransactionWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val channelName = "Transactions"
    private val channelIcon = R.drawable.ic_refresh
    private val transactionDatabase by lazy { AppDatabase.getInstance(context).transactionDataDao() }


    override suspend fun doWork(): Result {
        val transactionId = inputData.getLong("transactionId", 0)
        var transactionAttributes: TransactionAttributes? = null
        var isDeleted = false
        val repository = TransactionRepository(transactionDatabase, genericService?.create(TransactionService::class.java))
        runBlocking(Dispatchers.IO) {
            transactionAttributes = repository.getTransactionById(transactionId)[0].transactionAttributes
            isDeleted = repository.deleteTransactionById(transactionId)
        }
        if (isDeleted) {
            context.displayNotification(transactionAttributes?.description + " successfully deleted", channelName,
                    Constants.TRANSACTION_CHANNEL, channelIcon)
        } else {
            Result.failure()
            context.displayNotification("There was issue deleting ${transactionAttributes?.description}",
                    "Failed to delete transaction",
                    Constants.TRANSACTION_CHANNEL, channelIcon)
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