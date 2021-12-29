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

import android.accounts.AccountManager
import android.content.Context
import androidx.work.*
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.account.NewAccountManager
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.OAuthService
import xyz.hisname.fireflyiii.repository.models.auth.AuthModel
import java.util.concurrent.TimeUnit

class RefreshTokenWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters)  {

    companion object {
        // Worker migration from single user to multi user
        fun migrateWorkerV1ToV2(workManager: WorkManager, uuid: String, time: Long){
            val refreshWorker = workManager.getWorkInfosByTag("refresh_worker")
            refreshWorker.cancel(true)
            val data = Data.Builder()
                .putString("uuid", uuid)
                .build()
            val workBuilder = PeriodicWorkRequest
                .Builder(RefreshTokenWorker::class.java, time, TimeUnit.HOURS)
                .setInputData(data)
                .addTag("refresh_worker_$uuid")
                .addTag(uuid)
                .setConstraints(Constraints.Builder()
                    .setRequiresCharging(true)
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build())
                .build()
            workManager.enqueue(workBuilder)
        }
    }

    override suspend fun doWork(): Result {
        val networkCall: Response<AuthModel>?
        var workResult: Result
        try {
            val accManager = NewAccountManager(AccountManager.get(context), uuid)
            networkCall = genericService(uuid).create(OAuthService::class.java).getRefreshToken("refresh_token",
                    accManager.refreshToken, accManager.clientId,
                    accManager.secretKey)
            val authResponse = networkCall.body()
            if (authResponse != null && networkCall.isSuccessful) {
                accManager.accessToken = authResponse.access_token
                accManager.refreshToken = authResponse.refresh_token
                accManager.tokenExpiry = authResponse.expires_in
                FireflyClient.destroyInstance()
                workResult = Result.success()
            } else {
                workResult = Result.retry()
            }
        } catch (exception: Exception) {
            workResult = Result.retry()
        }
        return workResult
    }
}