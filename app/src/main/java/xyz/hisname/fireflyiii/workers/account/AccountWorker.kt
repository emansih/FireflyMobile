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
import android.net.Uri
import androidx.core.net.toUri
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.repository.account.AccountRepository
import xyz.hisname.fireflyiii.repository.attachment.AttachableType
import xyz.hisname.fireflyiii.repository.models.accounts.AccountAttributes
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.workers.BaseWorker
import xyz.hisname.fireflyiii.util.extension.showNotification
import xyz.hisname.fireflyiii.util.getUserEmail
import xyz.hisname.fireflyiii.workers.AttachmentWorker
import java.io.BufferedReader
import java.io.FileReader
import java.math.BigDecimal
import java.time.Duration
import java.util.concurrent.ThreadLocalRandom

class AccountWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters)  {

    private val channelIcon = R.drawable.ic_euro_sign

    companion object {

        fun initWorker(context: Context, accountName: String, accountType: String,
                       currencyCode: String?, iban: String?, bic: String?, accountNumber: String?,
                       openingBalance: String? = "0", openingBalanceDate: String?, accountRole: String?,
                       virtualBalance: String?, includeInNetWorth: Boolean, notes: String?, liabilityType: String?,
                       liabilityAmount: String?, liabilityStartDate: String?, interest: String?,
                       interestPeriod: String?, fileArray: ArrayList<Uri>){
            val accountData = Data.Builder()
                    .putString("name", accountName)
                    .putString("type", accountType)
                    .putString("currencyCode", currencyCode)
                    .putString("iban", iban)
                    .putString("bic", bic)
                    .putString("accountNumber", accountNumber)
                    .putString("openingBalance", openingBalance)
                    .putString("openingBalanceDate", openingBalanceDate)
                    .putString("virtualBalance", virtualBalance)
                    .putBoolean("includeNetWorth", includeInNetWorth)
                    .putString("accountRole", accountRole)
                    .putString("notes", notes)
                    .putString("liabilityType", liabilityType)
                    .putString("liabilityAmount", liabilityAmount)
                    .putString("liabilityStartDate", liabilityStartDate)
                    .putString("interest", interest)
                    .putString("interestPeriod", interestPeriod)
            if(fileArray.isNotEmpty()){
                accountData.putString("filesToUpload", fileArray.toString())
            }
            val accountTag =
                    WorkManager.getInstance(context).getWorkInfosByTag(
                            "add_periodic_account_$accountName" + "_" + accountType).get()
            if(accountTag == null || accountTag.size == 0){
                val appPref = AppPref(context.getSharedPreferences(context.getUserEmail() +
                        "-user-preferences", Context.MODE_PRIVATE))
                val delay = appPref.workManagerDelay
                val battery = appPref.workManagerLowBattery
                val networkType = appPref.workManagerNetworkType
                val requireCharging = appPref.workManagerRequireCharging
                val accountWork = PeriodicWorkRequestBuilder<AccountWorker>(Duration.ofMinutes(delay))
                        .setInputData(accountData.build())
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(networkType)
                                .setRequiresBatteryNotLow(battery)
                                .setRequiresCharging(requireCharging)
                                .build())
                        .addTag("add_periodic_account_$accountName" + "_" + accountType)
                        .build()
                WorkManager.getInstance(context).enqueue(accountWork)
                runBlocking(Dispatchers.IO) {
                    val bufferedReader = BufferedReader(FileReader(context.applicationInfo.dataDir + "/current_active_user.txt"))
                    val userEmail = bufferedReader.readLine()
                    val accountDatabase = AppDatabase.getInstance(context, userEmail).accountDataDao()
                    val currencyDatabase = AppDatabase.getInstance(context, userEmail).currencyDataDao()
                    val currencyData =
                                currencyDatabase.getCurrencyByCode(currencyCode ?: "")[0]
                    val currencySymbol = currencyData.currencyAttributes.symbol
                    val currencyId = currencyData.currencyId
                    val fakeAccountId = ThreadLocalRandom.current().nextLong()
                    accountDatabase.insert(AccountData(
                            fakeAccountId, AccountAttributes(
                            "","", accountName, true,
                            accountType, accountRole, currencyId, currencyCode, 0.toBigDecimal(),
                            currencySymbol, "", notes, "",
                            "", accountNumber,
                            iban, bic, 0.0,
                            BigDecimal(openingBalance), openingBalanceDate, liabilityType,
                            liabilityAmount, liabilityStartDate, interest, interestPeriod, includeInNetWorth, true)
                    ))
                }
            }
        }

        fun cancelWorker(accountName: String, accountType: String, context: Context){
            runBlocking(Dispatchers.IO) {
                val bufferedReader = BufferedReader(FileReader(context.applicationInfo.dataDir + "/current_active_user.txt"))
                val userEmail = bufferedReader.readLine()
                val accountDatabase = AppDatabase.getInstance(context, userEmail).accountDataDao()
                accountDatabase.deleteAccountByTypeAndName(accountType, accountName)
                accountDatabase
            }
            WorkManager.getInstance(context).cancelAllWorkByTag("add_periodic_account_$accountName" + "_" + accountType)
        }

    }

    override suspend fun doWork(): Result {
        val name = inputData.getString("name") ?: ""
        val accountType = inputData.getString("type") ?: ""
        val currencyCode = inputData.getString("currencyCode")
        val includeNetWorth = inputData.getBoolean("includeNetWorth",false)
        val accountRole = inputData.getString("accountRole")
        val liabilityType = inputData.getString("liabilityType")
        val liabilityAmount = inputData.getString("liabilityAmount")
        val liabilityStartDate = inputData.getString("liabilityStartDate")
        val interest = inputData.getString("interest")
        val interestPeriod = inputData.getString("interestPeriod")
        val accountNumber = inputData.getString("accountNumber")
        val iBanString = inputData.getString("iban")
        val bicString = inputData.getString("bic")
        val openingBalance = inputData.getString("openingBalance")
        val openingBalanceDate = inputData.getString("openingBalanceDate")
        val virtualBalance = inputData.getString("virtualBalance")
        val notes = inputData.getString("notes")
        val fileArray = inputData.getString("filesToUpload") ?: ""
        val accountRepository = AccountRepository(
                AppDatabase.getInstance(context, getCurrentUserEmail()).accountDataDao(),
                genericService.create(AccountsService::class.java)
        )
        val addAccount = accountRepository.addAccount(name, accountType, currencyCode,
                iBanString, bicString, accountNumber, openingBalance, openingBalanceDate,
                accountRole, virtualBalance, includeNetWorth, notes, liabilityType, liabilityAmount,
                liabilityStartDate, interest, interestPeriod)
        when {
            addAccount.response != null -> {
                cancelWorker(name,accountType, context)
                context.showNotification("Account Added", "$name was added successfully!", channelIcon)
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
                    AttachmentWorker.initWorker(arrayOfUri, addAccount.response.data.accountId,
                            context, AttachableType.Account)
                }
                return Result.success()
            }
            addAccount.errorMessage != null -> {
                context.showNotification("There was an error adding $name", addAccount.errorMessage, channelIcon)
                return Result.failure()
            }
            addAccount.error != null -> {
                return Result.retry()
            }
            else -> {
                return Result.failure()
            }
        }
    }
}