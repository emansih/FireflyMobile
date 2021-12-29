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

package xyz.hisname.fireflyiii.workers.piggybank

import android.content.Context
import androidx.work.*
import xyz.hisname.fireflyiii.data.remote.firefly.api.PiggybankService
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.piggybank.PiggyRepository
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.BaseWorker
import java.time.Duration

class DeletePiggyWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    companion object {
        fun initPeriodicWorker(piggyId: Long, context: Context, uuid: String){
            val piggyTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("delete_periodic_piggy_$piggyId" + "_$uuid").get()
            if (piggyTag == null || piggyTag.size == 0) {
                val piggyData = Data.Builder()
                    .putLong("piggyId", piggyId)
                    .putString("uuid", uuid)
                    .build()
                val appPref = AppPref(context.getSharedPreferences("$uuid-user-preferences", Context.MODE_PRIVATE))
                val delay = appPref.workManagerDelay
                val battery = appPref.workManagerLowBattery
                val networkType = appPref.workManagerNetworkType
                val requireCharging = appPref.workManagerRequireCharging
                val deletePiggyWork = PeriodicWorkRequestBuilder<DeletePiggyWorker>(Duration.ofMinutes(delay))
                    .setInputData(piggyData)
                    .addTag(uuid)
                    .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(networkType)
                                .setRequiresBatteryNotLow(battery)
                                .setRequiresCharging(requireCharging)
                                .build())
                        .addTag("delete_periodic_piggy_$piggyId")
                        .build()
                WorkManager.getInstance(context).enqueue(deletePiggyWork)
            }
        }

        fun cancelWorker(piggyId: Long, context: Context){
            WorkManager.getInstance(context).cancelAllWorkByTag("delete_periodic_piggy_$piggyId")
        }
    }

    override suspend fun doWork(): Result {
        val piggyDataBase = AppDatabase.getInstance(context, uuid).piggyDataDao()
        val piggyId = inputData.getLong("piggyId", 0)
        val repository = PiggyRepository(piggyDataBase, genericService(uuid).create(PiggybankService::class.java))
        return when (repository.deletePiggyById(piggyId)) {
            HttpConstants.NO_CONTENT_SUCCESS -> {
                cancelWorker(piggyId, context)
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