package xyz.hisname.fireflyiii.workers.piggybank

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
import xyz.hisname.fireflyiii.data.remote.firefly.api.PiggybankService
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyAttributes
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.workers.BaseWorker
import xyz.hisname.fireflyiii.ui.notifications.displayNotification
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import java.time.Duration
import java.util.concurrent.ThreadLocalRandom


class PiggyBankWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val channelIcon = R.drawable.ic_sort_descending

    override suspend fun doWork(): Result {
        val name = inputData.getString("name") ?: ""
        val accountId = inputData.getString("accountId") ?: ""
        val targetAmount = inputData.getString("targetAmount") ?: ""
        val currentAmount = inputData.getString("currentAmount")
        val startDate = inputData.getString("startDate") ?: ""
        val endDate = inputData.getString("endDate") ?: ""
        val notes = inputData.getString("notes") ?: ""
        val piggyWorkManagerId = inputData.getLong("piggyWorkManagerId", 0)
        genericService?.create(PiggybankService::class.java)?.createNewPiggyBank(name, accountId, targetAmount, currentAmount, startDate, endDate, notes)?.enqueue(retrofitCallback({ response ->
            var errorBody = ""
            if (response.errorBody() != null) {
                errorBody = String(response.errorBody()?.bytes()!!)
            }
            val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
            val responseBody = response.body()
            if (response.isSuccessful && responseBody != null) {
                context.displayNotification("$name was added successfully!", "Piggy Bank Added",
                        Constants.PIGGY_BANK_CHANNEL, channelIcon)
                cancelWorker(piggyWorkManagerId, context)
                runBlocking(Dispatchers.IO){
                    val piggyDatabase = AppDatabase.getInstance(context).piggyDataDao()
                    piggyDatabase.insert(responseBody.data)
                }
                Result.success()
            } else {
                var error = ""
                when {
                    gson.errors.name != null -> error = gson.errors.name[0]
                    gson.errors.account_id != null -> error = gson.errors.account_id[0]
                    gson.errors.current_amount != null -> error = gson.errors.current_amount[0]
                }
                cancelWorker(piggyWorkManagerId, context)
                context.displayNotification(error, "There was an issue adding $name",
                        Constants.PIGGY_BANK_CHANNEL, channelIcon)
                Result.failure()
            }
        })
        { throwable ->
            Result.retry()
        })
        return Result.success()
    }

    companion object {
        fun initWorker(context: Context, name: String, accountId: String, targetAmount: String,
                       currentAmount: String?, startDate: String?, endDate: String?, notes: String?){
            val piggyWorkManagerId = ThreadLocalRandom.current().nextLong()
            val piggyData = Data.Builder()
                    .putString("name", name)
                    .putString("accountId", accountId)
                    .putString("targetAmount", targetAmount)
                    .putString("currentAmount", currentAmount)
                    .putString("startDate", startDate)
                    .putString("endDate", endDate)
                    .putString("notes", notes)
                    .putLong("piggyWorkManagerId", piggyWorkManagerId)
                    .build()
            val piggyTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("add_piggy_periodic_$piggyWorkManagerId").get()
            if(piggyTag == null || piggyTag.size == 0){
                val delay = AppPref(PreferenceManager.getDefaultSharedPreferences(context)).workManagerDelay
                val battery = AppPref(PreferenceManager.getDefaultSharedPreferences(context)).workManagerLowBattery
                val piggyBankWork = PeriodicWorkRequestBuilder<PiggyBankWorker>(Duration.ofMinutes(delay))
                        .setInputData(piggyData)
                        .addTag("add_piggy_periodic_$piggyWorkManagerId")
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .setRequiresBatteryNotLow(battery)
                                .build())
                        .build()
                WorkManager.getInstance(context).enqueue(piggyBankWork)
                runBlocking(Dispatchers.IO){
                    val piggyDatabase = AppDatabase.getInstance(context).piggyDataDao()
                    val accountDatabase = AppDatabase.getInstance(context).accountDataDao()
                    val account = accountDatabase.getAccountById(accountId.toLong())[0]
                    val defaultCurrency = AppDatabase.getInstance(context).currencyDataDao().getDefaultCurrency()[0]
                    val currencyCode = defaultCurrency.currencyAttributes?.code
                    val currencySymbol = defaultCurrency.currencyAttributes?.symbol
                    val currencyDp = defaultCurrency.currencyAttributes?.decimal_places
                    val percentage = currentAmount?.toInt()?.div(targetAmount.toInt())?.times(100)
                    val leftToSave = targetAmount.toInt() - (currentAmount?.toInt() ?: 0)
                    piggyDatabase.insert(
                           PiggyData( "", piggyWorkManagerId, PiggyAttributes(
                                   "","", name, account.accountId,
                                   account.accountAttributes?.name, defaultCurrency.currencyId, currencyCode, currencySymbol,
                                   currencyDp, targetAmount.toBigDecimal(),  percentage,
                                   currentAmount?.toBigDecimal() ?: 0.toBigDecimal(),
                                   leftToSave.toBigDecimal(), 0.toBigDecimal(), startDate, endDate, 0, true, notes
                           ))
                    )
                }
            }
        }

        fun cancelWorker(piggyId: Long, context: Context){
            runBlocking(Dispatchers.IO){
                val piggyDatabase = AppDatabase.getInstance(context).piggyDataDao()
                piggyDatabase.deletePiggyById(piggyId)
            }
            WorkManager.getInstance(context).cancelAllWorkByTag("add_piggy_periodic_$piggyId")
        }
    }

}