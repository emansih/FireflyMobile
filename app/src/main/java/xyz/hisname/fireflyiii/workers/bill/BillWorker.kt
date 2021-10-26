/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.workers.bill

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.attachment.AttachableType
import xyz.hisname.fireflyiii.repository.bills.BillRepository
import xyz.hisname.fireflyiii.repository.models.bills.BillAttributes
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.workers.BaseWorker
import xyz.hisname.fireflyiii.util.extension.showNotification
import xyz.hisname.fireflyiii.util.getUniqueHash
import xyz.hisname.fireflyiii.workers.AttachmentWorker
import java.time.Duration
import java.time.LocalDate
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
        val fileArray = inputData.getString("filesToUpload") ?: ""
        val billWorkManagerId = inputData.getLong("billWorkManagerId", 0)
        val billRepository = BillRepository(
                AppDatabase.getInstance(context, getUniqueHash()).billDataDao(),
                genericService.create(BillsService::class.java)
        )

        val addBill = billRepository.addBill(name, minAmount,
                maxAmount, billDate, repeatFreq, skip,"1", currencyCode, notes)
        when {
            addBill.response != null -> {
                // Delete old data that we inserted when worker is being init
                billRepository.deleteBillById(billWorkManagerId)
                context.showNotification("Bill Added", context.getString(R.string.stored_new_bill, name), channelIcon)
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
                    AttachmentWorker.initWorker(arrayOfUri, addBill.response.data.billId,
                            context, AttachableType.BILL)
                }
                return Result.success()
            }
            addBill.errorMessage != null -> {
                context.showNotification("Error Adding $name", addBill.errorMessage, channelIcon)
                cancelWorker(billWorkManagerId, context)
                return Result.failure()
            }
            addBill.error != null -> {
               return Result.retry()
            }
            else -> {
                context.showNotification("Error Adding $name", "Please try again later", channelIcon)
                cancelWorker(billWorkManagerId, context)
                return Result.failure()
            }
        }
    }

    companion object {
        fun initWorker(context: Context, name: String, minAmount: String, maxAmount: String,
                       billDate: String, repeatFreq: String, skip: String, currencyCode: String,
                       notes: String?, fileArray: ArrayList<Uri>){
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
            if(fileArray.isNotEmpty()){
                billData.putString("filesToUpload", fileArray.toString())
            }
            val billTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("add_bill_periodic_$billWorkManagerId").get()
            if(billTag == null || billTag.size == 0) {
                val appPref = AppPref(context.getSharedPreferences(context.getUniqueHash().toString() + "-user-preferences", Context.MODE_PRIVATE))
                val delay = appPref.workManagerDelay
                val battery = appPref.workManagerLowBattery
                val networkType = appPref.workManagerNetworkType
                val requireCharging = appPref.workManagerRequireCharging
                val billWork = PeriodicWorkRequestBuilder<BillWorker>(Duration.ofMinutes(delay))
                        .setInputData(billData.build())
                        .addTag("add_bill_periodic_$billWorkManagerId")
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(networkType)
                                .setRequiresBatteryNotLow(battery)
                                .setRequiresCharging(requireCharging)
                                .build())
                        .build()
                WorkManager.getInstance(context).enqueue(billWork)
                runBlocking(Dispatchers.IO){
                    val billDatabase = AppDatabase.getInstance(context, context.getUniqueHash()).billDataDao()
                    val currencyDatabase = AppDatabase.getInstance(context, context.getUniqueHash()).currencyDataDao()
                    val currency = currencyDatabase.getCurrencyByCode(currencyCode)[0]
                    billDatabase.insert(
                            BillData(
                                    billWorkManagerId, BillAttributes("","",
                                    name, currency.currencyId, currencyCode,
                                    currency.currencyAttributes.symbol,
                                    currency.currencyAttributes.decimal_places,
                                    minAmount.toBigDecimal(), maxAmount.toBigDecimal(), LocalDate.parse(billDate),
                                    repeatFreq, skip.toInt(), true, 0, listOf(),
                                    listOf(), notes, null, true)
                            )
                    )
                }
            }
        }

        fun cancelWorker(billId: Long, context: Context){
            runBlocking(Dispatchers.IO){
                val billDatabase = AppDatabase.getInstance(context, context.getUniqueHash()).billDataDao()
                billDatabase.deleteBillById(billId)
            }
            WorkManager.getInstance(context).cancelAllWorkByTag("add_bill_periodic_$billId")
        }
    }
}