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

package xyz.hisname.fireflyiii.workers.account

import android.content.Context
import androidx.work.*
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.account.AccountRepository
import xyz.hisname.fireflyiii.util.getUniqueHash
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.BaseWorker
import java.time.Duration

class DeleteAccountWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val accountDatabase by lazy { AppDatabase.getInstance(context, getUniqueHash()).accountDataDao() }

    companion object {
        fun initPeriodicWorker(accountId: Long, context: Context){
            val accountTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("delete_periodic_account_$accountId").get()
            if(accountTag == null || accountTag.size == 0) {
                val accountData = Data.Builder()
                        .putLong("accountId", accountId)
                        .build()
                val appPref = AppPref(context.getSharedPreferences(context.getUniqueHash().toString() +
                        "-user-preferences", Context.MODE_PRIVATE))
                val delay = appPref.workManagerDelay
                val battery = appPref.workManagerLowBattery
                val networkType = appPref.workManagerNetworkType
                val requireCharging = appPref.workManagerRequireCharging
                val deleteAccountWork = PeriodicWorkRequestBuilder<DeleteAccountWorker>(Duration.ofMinutes(delay))
                        .setInputData(accountData)
                        .addTag("delete_periodic_account_$accountId")
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(networkType)
                                .setRequiresBatteryNotLow(battery)
                                .setRequiresCharging(requireCharging)
                                .build())
                        .build()
                WorkManager.getInstance(context).enqueue(deleteAccountWork)
            }
        }

        fun cancelWorker(accountId: Long, context: Context){
            WorkManager.getInstance(context).cancelAllWorkByTag("delete_periodic_account_$accountId")
        }
    }

    override suspend fun doWork(): Result {
        val accountId = inputData.getLong("accountId", 0L)
        val accountService = genericService.create(AccountsService::class.java)
        val repository = AccountRepository(accountDatabase, accountService)
        return when(repository.deleteAccountById(accountId)){
            HttpConstants.NO_CONTENT_SUCCESS -> {
                cancelWorker(accountId, context)
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