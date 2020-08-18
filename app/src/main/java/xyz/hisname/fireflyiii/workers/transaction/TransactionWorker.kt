package xyz.hisname.fireflyiii.workers.transaction

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.ui.notifications.displayNotification
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import xyz.hisname.fireflyiii.workers.BaseWorker
import java.time.Duration
import java.util.concurrent.TimeUnit

class TransactionWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val channelIcon = R.drawable.ic_refresh

    override suspend fun doWork(): Result {
        val transactionType = inputData.getString("transactionType") ?: ""
        val transactionDescription = inputData.getString("description") ?: ""
        val transactionDate = inputData.getString("date") ?: ""
        val transactionAmount = inputData.getString("amount") ?: ""
        val transactionCurrency = inputData.getString("currency") ?: ""
        val destinationName = inputData.getString("destinationName")
        val sourceName = inputData.getString("sourceName")
        val piggyBank = inputData.getString("piggyBankName")
        val category = inputData.getString("categoryName")
        val tags = inputData.getString("tags")
        val budget = inputData.getString("budgetName")
        val transactionWorkManagerId = inputData.getLong("transactionWorkManagerId", 0)
        genericService?.create(TransactionService::class.java)?.addTransaction(convertString(transactionType),
                transactionDescription, transactionDate, piggyBank, transactionAmount,sourceName,
                destinationName, transactionCurrency, category, tags, budget)?.enqueue(retrofitCallback({ response ->
                    var errorBody = ""
                    if (response.errorBody() != null) {
                        errorBody = String(response.errorBody()?.bytes()!!)
                    }
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    if (response.isSuccessful) {
                        WorkManager.getInstance(context).cancelAllWorkByTag("transactionWorker")
                        context.displayNotification("Transaction added successfully!", transactionType,
                                Constants.TRANSACTION_CHANNEL, channelIcon)
                        cancelWorker(transactionWorkManagerId, context)
                        Result.success()
                    } else {
                        var error = ""
                        when {
                            gson.errors.transactions_destination_name != null -> {
                                error = gson.errors.transactions_destination_name[0]
                            }
                            gson.errors.transactions_currency != null -> {
                                error = gson.errors.transactions_currency[0]
                            }
                            gson.errors.transactions_source_name != null -> {
                                error = gson.errors.transactions_source_name[0]
                            }
                        }
                        WorkManager.getInstance(context).cancelAllWorkByTag(transactionWorkManagerId.toString())
                        context.displayNotification(error, "Error Adding $transactionType",
                                Constants.TRANSACTION_CHANNEL, channelIcon)
                        cancelWorker(transactionWorkManagerId, context)
                        Result.failure()
                    }
                })
                { throwable ->
                    /*context.displayNotification(throwable.message.toString(),
                            "Error Adding $transactionType", Constants.TRANSACTION_CHANNEL, channelIcon)*/
                    Result.retry()
                })
        return Result.success()
    }

    private fun convertString(type: String) = type.substring(0,1).toLowerCase() + type.substring(1).toLowerCase()

    companion object {
        fun initWorker(context: Context, dataBuilder: Data.Builder, type: String, transactionWorkManagerId: Long) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val workManagerDelay = AppPref(sharedPref).workManagerDelay
            val transactionTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("add_periodic_transaction_$transactionWorkManagerId").get()
            if (transactionTag == null || transactionTag.size == 0) {
                dataBuilder.putString("transactionType", type)
                val transactionWork = PeriodicWorkRequestBuilder<TransactionWorker>(Duration.ofMinutes(workManagerDelay))
                        .setInputData(dataBuilder.build())
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .setRequiresBatteryNotLow(true)
                                .build())
                        .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
                        .addTag("add_periodic_transaction_-$transactionWorkManagerId")
                        .build()
                WorkManager.getInstance(context).enqueueUniquePeriodicWork("add_periodic_transaction_-$transactionWorkManagerId",
                        ExistingPeriodicWorkPolicy.REPLACE, transactionWork)
            }
        }

        fun cancelWorker(fakeTransactionId: Long, context: Context){
            runBlocking(Dispatchers.IO) {
                val transactionDatabase = AppDatabase.getInstance(context).transactionDataDao()
                transactionDatabase.deleteTransactionByJournalId(fakeTransactionId)
            }
            WorkManager.getInstance(context).cancelAllWorkByTag("add_periodic_transaction_$fakeTransactionId")
        }

    }
}