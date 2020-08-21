package xyz.hisname.fireflyiii.workers

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.*
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import java.time.Duration
import java.util.concurrent.TimeUnit

class DeleteCurrencyWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters)  {

    private val currencyDatabase by lazy { AppDatabase.getInstance(context).currencyDataDao() }

    companion object {
        fun initPeriodicWorker(currencyId: Long, context: Context) {
            val currencyTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("delete_currency_periodic$currencyId").get()
            if (currencyTag == null || currencyTag.size == 0) {
                val currencyData = Data.Builder()
                        .putLong("currencyId", currencyId)
                        .build()
                val delay = AppPref(PreferenceManager.getDefaultSharedPreferences(context)).workManagerDelay
                val battery = AppPref(PreferenceManager.getDefaultSharedPreferences(context)).workManagerLowBattery
                val deleteCurrencyWork = PeriodicWorkRequestBuilder<DeleteCurrencyWorker>(Duration.ofMinutes(delay))
                        .setInputData(currencyData)
                        .addTag("delete_currency_periodic$currencyId")
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .setRequiresBatteryNotLow(battery)
                                .build())
                        .build()
                WorkManager.getInstance(context).enqueue(deleteCurrencyWork)
            }
        }
    }

    override suspend fun doWork(): Result {
        val currencyId = inputData.getLong("currencyId", 0)
        val repository = CurrencyRepository(currencyDatabase, genericService?.create(CurrencyService::class.java))
        val currencyName = repository.getCurrencyById(currencyId)[0].currencyAttributes?.name ?: ""
        repository.deleteCurrencyByName(currencyName)
        return Result.success()
    }


}