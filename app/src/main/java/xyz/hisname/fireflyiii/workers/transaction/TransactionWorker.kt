package xyz.hisname.fireflyiii.workers.transaction

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf
import androidx.preference.PreferenceManager
import androidx.work.*
import com.google.gson.Gson
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.iconics.utils.toAndroidIconCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionActivity
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.showNotification
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import xyz.hisname.fireflyiii.workers.BaseWorker
import java.time.Duration

class TransactionWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val channelIcon = R.drawable.ic_refresh

    override suspend fun doWork(): Result {
        val transactionType = inputData.getString("transactionType") ?: ""
        val transactionDescription = inputData.getString("description") ?: ""
        val transactionDate = inputData.getString("date") ?: ""
        val transactionTime = inputData.getString("time")
        val transactionAmount = inputData.getString("amount") ?: ""
        val transactionCurrency = inputData.getString("currency") ?: ""
        val destinationName = inputData.getString("destinationName")
        val sourceName = inputData.getString("sourceName")
        val piggyBank = inputData.getString("piggyBankName")
        val category = inputData.getString("categoryName")
        val tags = inputData.getString("tags")
        val budget = inputData.getString("budgetName")
        val dateTime = if (transactionTime == null) {
            transactionDate
        } else {
            DateTimeUtil.mergeDateTimeToIso8601(transactionDate, transactionTime)
        }
        val transactionWorkManagerId = inputData.getLong("transactionWorkManagerId", 0)
        genericService?.create(TransactionService::class.java)?.addTransaction(convertString(transactionType),
                transactionDescription, dateTime, piggyBank, transactionAmount, sourceName,
                destinationName, transactionCurrency, category, tags, budget)?.enqueue(retrofitCallback({ response ->
            var errorBody = ""
            if (response.errorBody() != null) {
                errorBody = String(response.errorBody()?.bytes()!!)
            }
            val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
            if (response.isSuccessful) {
                context.showNotification(transactionType, "Transaction added successfully!", channelIcon)
                cancelWorker(transactionWorkManagerId, context)
                response.body()?.data?.transactionAttributes?.transactions?.forEachIndexed { _, transaction ->
                    runBlocking(Dispatchers.IO) {
                        val transactionDatabase = AppDatabase.getInstance(context).transactionDataDao()
                        transactionDatabase.insert(transaction)
                        transactionDatabase.insert(TransactionIndex(response.body()?.data?.transactionId,
                                transaction.transaction_journal_id))
                    }
                }
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
                val transactionActivity = Intent(context, AddTransactionActivity::class.java).apply {
                    bundleOf("transactionType" to transactionType,
                            "transactionDescription" to transactionDescription,
                            "transactionAmount" to transactionAmount,
                            "transactionTime" to transactionTime,
                            "transactionDate" to transactionDate,
                            "transactionPiggyBank" to piggyBank,
                            "transactionSourceAccount" to sourceName,
                            "transactionDestinationAccount" to destinationName,
                            "transactionTags" to tags,
                            "transactionBudget" to budget,
                            "transactionCategory" to category,
                            "isFromNotification" to true)
                }
                val icon = IconicsDrawable(context).apply {
                    icon = FontAwesome.Icon.faw_edit
                    sizeDp = 24
                }.toAndroidIconCompat()
                context.showNotification("Error Adding $transactionType",
                        error, channelIcon,
                        PendingIntent.getActivity(context, 0, transactionActivity, 0),
                        context.getString(R.string.edit), icon)
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

    private fun convertString(type: String) = type.substring(0, 1).toLowerCase() + type.substring(1).toLowerCase()

    companion object {
        fun initWorker(context: Context, dataBuilder: Data.Builder, type: String, transactionWorkManagerId: Long) {
            val transactionTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("add_periodic_transaction_$transactionWorkManagerId").get()
            if (transactionTag == null || transactionTag.size == 0) {
                val appPref = AppPref(PreferenceManager.getDefaultSharedPreferences(context))
                val delay = appPref.workManagerDelay
                val battery = appPref.workManagerLowBattery
                val networkType = appPref.workManagerNetworkType
                val requireCharging = appPref.workManagerRequireCharging
                dataBuilder.putString("transactionType", type)
                val transactionWork = PeriodicWorkRequestBuilder<TransactionWorker>(Duration.ofMinutes(delay))
                        .setInputData(dataBuilder.build())
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(networkType)
                                .setRequiresBatteryNotLow(battery)
                                .setRequiresCharging(requireCharging)
                                .build())
                        .addTag("add_periodic_transaction_$transactionWorkManagerId")
                        .build()
                WorkManager.getInstance(context).enqueue(transactionWork)
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