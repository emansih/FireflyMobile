package xyz.hisname.fireflyiii.workers.bill

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.*
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.models.bills.BillAttributes
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.workers.BaseWorker
import xyz.hisname.fireflyiii.util.extension.showNotification
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import java.time.Duration
import java.util.concurrent.ThreadLocalRandom

class BillWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val channelIcon = R.drawable.ic_calendar_blank

    override suspend fun doWork(): Result {
        val name = inputData.getString("name") ?: ""
        val minAmount = inputData.getString("minAmount") ?: ""
        val maxAmount = inputData.getString("maxAmount") ?: ""
        val billDate = inputData.getString("billDate") ?: ""
        val repeatFreq = inputData.getString("repeatFreq") ?: ""
        val skip = inputData.getString("skip") ?: ""
        val currencyCode = inputData.getString("currencyCode") ?: ""
        val notes = inputData.getString("notes")
        val billWorkManagerId = inputData.getLong("billWorkManagerId", 0)
        genericService?.create(BillsService::class.java)?.createBill(name, minAmount,
                maxAmount, billDate, repeatFreq, skip,"1", currencyCode, notes)?.enqueue(
                retrofitCallback({ response ->
                    var errorBody = ""
                    var error = ""
                    if (response.errorBody() != null) {
                        errorBody = String(response.errorBody()?.bytes()!!)
                    }
                    val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorBody)
                    val responseBody = response.body()
                    if (response.isSuccessful && responseBody != null) {
                        context.showNotification("Bill Added", "$name added successfully!", channelIcon)
                        cancelWorker(billWorkManagerId, context)
                        runBlocking(Dispatchers.IO){
                            val billDatabase = AppDatabase.getInstance(context).billDataDao()
                            billDatabase.insert(responseBody.data)
                        }
                        Result.success()
                    } else {
                        when {
                            moshi?.errors?.name != null -> error = moshi.errors.name[0]
                            moshi?.errors?.currency_code != null -> error = moshi.errors.currency_code[0]
                            moshi?.errors?.amount_min != null -> error = moshi.errors.amount_min[0]
                            moshi?.errors?.repeat_freq != null -> error = moshi.errors.repeat_freq[0]
                        }
                        context.showNotification("Error Adding $name", error, channelIcon)
                        cancelWorker(billWorkManagerId, context)
                        Result.failure()
                    }
                })
                { throwable ->
                    Result.retry()
                })
        return Result.success()
    }

    companion object {
        fun initWorker(context: Context, name: String, minAmount: String, maxAmount: String,
                       billDate: String, repeatFreq: String, skip: String, currencyCode: String, notes: String?){
            val billWorkManagerId = ThreadLocalRandom.current().nextLong()
            val billData = Data.Builder()
                    .putString("name", name)
                    .putString("minAmount", minAmount)
                    .putString("maxAmount", maxAmount)
                    .putString("billDate", billDate)
                    .putString("repeatFreq", repeatFreq)
                    .putString("skip", skip)
                    .putString("currencyCode", currencyCode)
                    .putString("notes", notes)
                    .putLong("billWorkManagerId", billWorkManagerId)
                    .build()
            val billTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("add_bill_periodic_$billWorkManagerId").get()
            if(billTag == null || billTag.size == 0) {
                val appPref = AppPref(PreferenceManager.getDefaultSharedPreferences(context))
                val delay = appPref.workManagerDelay
                val battery = appPref.workManagerLowBattery
                val networkType = appPref.workManagerNetworkType
                val requireCharging = appPref.workManagerRequireCharging
                val billWork = PeriodicWorkRequestBuilder<BillWorker>(Duration.ofMinutes(delay))
                        .setInputData(billData)
                        .addTag("add_bill_periodic_$billWorkManagerId")
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(networkType)
                                .setRequiresBatteryNotLow(battery)
                                .setRequiresCharging(requireCharging)
                                .build())
                        .build()
                WorkManager.getInstance(context).enqueue(billWork)
                runBlocking(Dispatchers.IO){
                    val billDatabase = AppDatabase.getInstance(context).billDataDao()
                    val currencyDatabase = AppDatabase.getInstance(context).currencyDataDao()
                    val currency = currencyDatabase.getCurrencyByCode(currencyCode)[0]
                    billDatabase.insert(
                            BillData(
                                    billWorkManagerId, BillAttributes("","",
                                    name, currency.currencyId ?: 0L, currencyCode,
                                    currency.currencyAttributes?.symbol ?: "",
                                    currency.currencyAttributes?.decimal_places ?: 0,
                                    minAmount.toBigDecimal(), maxAmount.toBigDecimal(), billDate,
                                    repeatFreq, skip.toInt(), true, 0, listOf(),
                                    listOf(), notes, null, true)
                            )
                    )
                }
            }
        }

        fun cancelWorker(billId: Long, context: Context){
            runBlocking(Dispatchers.IO){
                val billDatabase = AppDatabase.getInstance(context).billDataDao()
                billDatabase.deleteBillById(billId)
            }
            WorkManager.getInstance(context).cancelAllWorkByTag("add_bill_periodic_$billId")
        }
    }
}