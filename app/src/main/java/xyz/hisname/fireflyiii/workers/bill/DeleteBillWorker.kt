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
import androidx.work.*
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.bills.BillRepository
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.BaseWorker
import java.time.Duration

class DeleteBillWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {


    companion object {
        fun initPeriodicWorker(billId: Long, context: Context, uuid: String){
            val billTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("delete_bill_periodic_$billId" + "_$uuid").get()
            if(billTag == null || billTag.size == 0) {
                val billData = Data.Builder()
                    .putLong("billId", billId)
                    .putString("uuid", uuid)
                    .build()
                val appPref = AppPref(context.getSharedPreferences(uuid +
                        "-user-preferences", Context.MODE_PRIVATE))
                val delay = appPref.workManagerDelay
                val battery = appPref.workManagerLowBattery
                val networkType = appPref.workManagerNetworkType
                val requireCharging = appPref.workManagerRequireCharging
                val deleteBillWork = PeriodicWorkRequestBuilder<DeleteBillWorker>(Duration.ofMinutes(delay))
                        .setInputData(billData)
                        .addTag("delete_bill_periodic_$billId" + "_$uuid")
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(networkType)
                                .setRequiresBatteryNotLow(battery)
                                .setRequiresCharging(requireCharging)
                                .build())
                        .build()
                WorkManager.getInstance(context).enqueue(deleteBillWork)
            }
        }

        fun cancelWorker(billId: Long, context: Context, uuid: String){
            WorkManager.getInstance(context).cancelAllWorkByTag("delete_bill_periodic_$billId" + "_$uuid")
        }
    }

    override suspend fun doWork(): Result {
        val billDatabase =  AppDatabase.getInstance(context, uuid).billDataDao()
        val billId = inputData.getLong("billId", 0)
        val billService = genericService(uuid).create(BillsService::class.java)
        val repository = BillRepository(billDatabase, billService)
        return when(repository.deleteBillById(billId)){
            HttpConstants.NO_CONTENT_SUCCESS -> {
                cancelWorker(billId, context, uuid)
                Result.success()
            }
            HttpConstants.FAILED -> {
                Result.retry()
            }
            else -> {
                Result.failure()
            }
        }
    }


}