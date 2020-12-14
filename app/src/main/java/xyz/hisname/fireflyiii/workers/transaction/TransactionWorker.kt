package xyz.hisname.fireflyiii.workers.transaction

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.preference.PreferenceManager
import androidx.work.*
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.iconics.utils.toAndroidIconCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.attachment.AttachableType
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionActivity
import xyz.hisname.fireflyiii.util.extension.showNotification
import xyz.hisname.fireflyiii.workers.AttachmentWorker
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
        val notes = inputData.getString("notes")
        val fileArray = inputData.getString("filesToUpload") ?: ""
        val transactionWorkManagerId = inputData.getLong("transactionWorkManagerId", 0)
        val transactionRepository = TransactionRepository(
                AppDatabase.getInstance(context).transactionDataDao(),
                genericService.create(TransactionService::class.java)
        )
        val addTransaction = transactionRepository.addTransaction(transactionType, transactionDescription, transactionDate,
                transactionTime, piggyBank, transactionAmount, sourceName,
                destinationName, transactionCurrency, category, tags, budget, notes)

        when {
            addTransaction.response != null -> {
                // Delete old data that we inserted when worker is being init
                transactionRepository.deleteTransactionById(transactionWorkManagerId)
                context.showNotification(transactionType, context.resources.getString(R.string.transaction_added), channelIcon)
                if(fileArray.isNotEmpty()){
                    // Remove [ and ] in the string
                    val beforeArray = fileArray.substring(1)
                    val modifiedArray = beforeArray.substring(0, beforeArray.length - 1)
                    val arrayOfString = modifiedArray.split(",")
                    val arrayOfUri = arrayListOf<Uri>()
                    arrayOfString.forEach { array ->
                        // Remove white spaces. First element does not have white spaces however,
                        // subsequent elements has it
                        arrayOfUri.add(array.replace("\\s".toRegex(), "").toUri())
                    }
                    var journalId = 0L
                    addTransaction.response.data.transactionAttributes.transactions.forEach { transactions ->
                        journalId = transactions.transaction_journal_id
                    }
                    AttachmentWorker.initWorker(arrayOfUri, journalId,
                            context, AttachableType.TRANSACTION)
                }
                return Result.success()
            }
            addTransaction.errorMessage != null -> {
                val transactionIntent = Intent(context, AddTransactionActivity::class.java)
                val bundleToPass =  bundleOf("transactionType" to transactionType,
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
                        "transactionNotes" to notes,
                        "isFromNotification" to true)
                transactionIntent.putExtras(bundleToPass)
                val icon = IconicsDrawable(context).apply {
                    icon = FontAwesome.Icon.faw_edit
                    sizeDp = 24
                }.toAndroidIconCompat()
                context.showNotification("Error Adding $transactionDescription",
                        addTransaction.errorMessage, channelIcon,
                        PendingIntent.getActivity(context, 0, transactionIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT),
                        context.getString(R.string.edit), icon)
                cancelWorker(transactionWorkManagerId, context)
                return Result.failure()
            }
            addTransaction.error != null -> {
                context.showNotification("Error Adding $transactionDescription", "Please try again later", channelIcon)
                cancelWorker(transactionWorkManagerId, context)
                return Result.retry()
            }
            else -> {
                return Result.failure()
            }
        }
    }

    companion object {
        fun initWorker(context: Context, dataBuilder: Data.Builder,
                       type: String, transactionWorkManagerId: Long, fileArray: ArrayList<Uri>) {
            val transactionTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("add_periodic_transaction_$transactionWorkManagerId").get()
            if (transactionTag == null || transactionTag.size == 0) {
                val appPref = AppPref(PreferenceManager.getDefaultSharedPreferences(context))
                val delay = appPref.workManagerDelay
                val battery = appPref.workManagerLowBattery
                val networkType = appPref.workManagerNetworkType
                val requireCharging = appPref.workManagerRequireCharging
                dataBuilder.putString("transactionType", type)
                if(fileArray.isNotEmpty()){
                    dataBuilder.putString("filesToUpload", fileArray.toString())
                }
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