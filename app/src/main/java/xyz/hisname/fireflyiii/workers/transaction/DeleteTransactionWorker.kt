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
import androidx.preference.PreferenceManager
import androidx.work.*
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.BaseWorker
import java.time.Duration
import java.util.concurrent.TimeUnit

class DeleteTransactionWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val transactionDatabase by lazy { AppDatabase.getInstance(context).transactionDataDao() }

    override suspend fun doWork(): Result {
        val transactionId = inputData.getLong("transactionId", 0)
        val repository = TransactionRepository(transactionDatabase, genericService.create(TransactionService::class.java))
        return when (repository.deleteTransactionById(transactionId)) {
            HttpConstants.NO_CONTENT_SUCCESS -> {
                cancelWorker(transactionId, context)
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
        fun setupWorker(transactionId: Long, context: Context){
            val transactionTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("delete_periodic_transaction_$transactionId").get()
            if (transactionTag == null || transactionTag.size == 0) {
                val transactionData = Data.Builder()
                        .putLong("transactionId", transactionId)
                        .build()
                val appPref = AppPref(PreferenceManager.getDefaultSharedPreferences(context))
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

        fun cancelWorker(transactionId: Long, context: Context){
            WorkManager.getInstance(context).cancelAllWorkByTag("delete_periodic_transaction_$transactionId")
        }
    }

}