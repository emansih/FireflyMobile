package xyz.hisname.fireflyiii.workers.piggybank

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.PiggybankService
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyAttributes
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.piggybank.PiggyRepository
import xyz.hisname.fireflyiii.util.extension.showNotification
import xyz.hisname.fireflyiii.workers.BaseWorker
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
        val piggyRepository = PiggyRepository(AppDatabase.getInstance(context).piggyDataDao(),
                genericService?.create(PiggybankService::class.java))
        val addPiggy = piggyRepository.addPiggyBank(name, accountId.toLong(), targetAmount,
                currentAmount, startDate, endDate, notes)
        when {
            addPiggy.response != null -> {
                // Delete old data that we inserted when worker is being init
                piggyRepository.deletePiggyById(piggyWorkManagerId)
                context.showNotification("Piggy bank added","Stored new piggy bank $name", channelIcon)
                return Result.success()
            }
            addPiggy.errorMessage != null -> {
                context.showNotification("Error Adding $name", addPiggy.errorMessage, channelIcon)
                cancelWorker(piggyWorkManagerId, context)
                return Result.failure()
            }
            addPiggy.error != null -> {
                return Result.retry()
            }
            else -> {
                context.showNotification("Error Adding $name", "Please try again later", channelIcon)
                cancelWorker(piggyWorkManagerId, context)
                return Result.failure()
            }
        }
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
                val appPref = AppPref(PreferenceManager.getDefaultSharedPreferences(context))
                val delay = appPref.workManagerDelay
                val battery = appPref.workManagerLowBattery
                val networkType = appPref.workManagerNetworkType
                val requireCharging = appPref.workManagerRequireCharging
                val piggyBankWork = PeriodicWorkRequestBuilder<PiggyBankWorker>(Duration.ofMinutes(delay))
                        .setInputData(piggyData)
                        .addTag("add_piggy_periodic_$piggyWorkManagerId")
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(networkType)
                                .setRequiresBatteryNotLow(battery)
                                .setRequiresCharging(requireCharging)
                                .build())
                        .build()
                WorkManager.getInstance(context).enqueue(piggyBankWork)
                runBlocking(Dispatchers.IO){
                    val piggyDatabase = AppDatabase.getInstance(context).piggyDataDao()
                    val accountDatabase = AppDatabase.getInstance(context).accountDataDao()
                    val account = accountDatabase.getAccountById(accountId.toLong())
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
                                   leftToSave.toBigDecimal(), 0.toBigDecimal(), startDate, endDate, 0, true, notes, true
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