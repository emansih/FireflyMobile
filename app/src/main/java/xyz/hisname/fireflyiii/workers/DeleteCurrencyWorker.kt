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

package xyz.hisname.fireflyiii.workers

import android.content.Context
import androidx.work.*
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import java.time.Duration

class DeleteCurrencyWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters)  {

    companion object {
        fun initPeriodicWorker(currencyId: Long, context: Context, uuid: String) {
            val currencyTag =
                WorkManager.getInstance(context)
                    .getWorkInfosByTag("delete_currency_periodic$currencyId" + "_$uuid").get()
            if (currencyTag == null || currencyTag.size == 0) {
                val currencyData = Data.Builder()
                    .putLong("currencyId", currencyId)
                    .putString("uuid", uuid)
                    .build()
                val appPref = AppPref(context.getSharedPreferences("$uuid-user-preferences", Context.MODE_PRIVATE))
                val delay = appPref.workManagerDelay
                val battery = appPref.workManagerLowBattery
                val networkType = appPref.workManagerNetworkType
                val requireCharging = appPref.workManagerRequireCharging
                val deleteCurrencyWork =
                    PeriodicWorkRequestBuilder<DeleteCurrencyWorker>(Duration.ofMinutes(delay))
                        .setInputData(currencyData)
                        .addTag("delete_currency_periodic$currencyId"  + "_$uuid")
                        .addTag(uuid)
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiredNetworkType(networkType)
                                .setRequiresBatteryNotLow(battery)
                                .setRequiresCharging(requireCharging)
                                .build()
                        )
                        .build()
                WorkManager.getInstance(context).enqueue(deleteCurrencyWork)
            }
        }
    }

    override suspend fun doWork(): Result {
        val currencyDatabase =  AppDatabase.getInstance(context, uuid).currencyDataDao()
        val currencyId = inputData.getLong("currencyId", 0)
        val repository = CurrencyRepository(currencyDatabase, genericService(uuid).create(CurrencyService::class.java))
        val currencyCode = repository.getCurrencyById(currencyId)[0].currencyAttributes.code
        repository.deleteCurrencyByCode(currencyCode)
        return Result.success()
    }


}