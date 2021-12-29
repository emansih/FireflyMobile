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

package xyz.hisname.fireflyiii.workers.transaction

import android.content.Context
import androidx.work.*
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.BaseWorker
import java.time.Duration

class DeleteTransactionWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val transactionDatabase =  AppDatabase.getInstance(context, uuid).transactionDataDao()
        val transactionId = inputData.getLong("transactionId", 0)
        val repository = TransactionRepository(transactionDatabase, genericService(uuid).create(TransactionService::class.java))
        return when (repository.deleteTransactionById(transactionId)) {
            HttpConstants.NO_CONTENT_SUCCESS -> {
                cancelWorker(transactionId, context, uuid)
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


    companion object {
        fun setupWorker(transactionId: Long, context: Context, uuid: String){
            val transactionTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("delete_periodic_transaction_$transactionId" + "_$uuid").get()
            if (transactionTag == null || transactionTag.size == 0) {
                val transactionData = Data.Builder()
                    .putLong("transactionId", transactionId)
                    .putString("uuid", uuid)
                    .build()
                val appPref = AppPref(context.getSharedPreferences("$uuid-user-preferences", Context.MODE_PRIVATE))
                val delay = appPref.workManagerDelay
                val battery = appPref.workManagerLowBattery
                val networkType = appPref.workManagerNetworkType
                val requireCharging = appPref.workManagerRequireCharging
                val transactionWork = PeriodicWorkRequestBuilder<DeleteTransactionWorker>(Duration.ofMinutes(delay))
                        .setInputData(transactionData)
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(networkType)
                                .setRequiresBatteryNotLow(battery)
                                .setRequiresCharging(requireCharging)
                                .build())
                        .build()
                WorkManager.getInstance(context).enqueue(transactionWork)
            }

        }

        fun cancelWorker(transactionId: Long, context: Context, uuid: String){
            WorkManager.getInstance(context).cancelAllWorkByTag("delete_periodic_transaction_$transactionId" + "_$uuid")
        }
    }

}