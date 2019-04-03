package xyz.hisname.fireflyiii.workers.account

import android.content.Context
import androidx.work.*
import com.google.gson.Gson
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.remote.api.AccountsService
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.workers.BaseWorker
import xyz.hisname.fireflyiii.ui.notifications.displayNotification
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class AccountWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters)  {

    private val channelIcon = R.drawable.ic_euro_sign

    companion object {
        fun initWorker(accountName: String, accountType: String,
                       currencyCode: String?, iban: String?, bic: String?, accountNumber: String?,
                       openingBalance: String?, openingBalanceDate: String?, accountRole: String?,
                       virtualBalance: String?, includeInNetWorth: Boolean, notes: String?, liabilityType: String?,
                       liabilityAmount: String?, liabilityStartDate: String?, interest: String?, interestPeriod: String?){
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
                    .build()
            val accountWork = OneTimeWorkRequest.Builder(AccountWorker::class.java)
                    .setInputData(accountData)
                    .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build()
            WorkManager.getInstance().enqueue(accountWork)
        }
    }

    override fun doWork(): Result {
        val name = inputData.getString("name") ?: ""
        val accountType = inputData.getString("type") ?: ""
        val currencyCode = inputData.getString("currencyCode") ?: ""
        val includeNetWorth = inputData.getBoolean("includeNetWorth",false)
        val accountRole = inputData.getString("accountRole") ?: ""
        val liabilityType = inputData.getString("liabilityType") ?: ""
        val liabilityAmount = inputData.getString("liabilityAmount") ?: ""
        val liabilityStartDate = inputData.getString("liabilityStartDate") ?: ""
        val interest = inputData.getString("interest") ?: ""
        val interestPeriod = inputData.getString("interestPeriod") ?: ""
        val accountNumber = inputData.getString("accountNumber") ?: ""
        val iBanString = inputData.getString("iban") ?: ""
        val bicString = inputData.getString("bic") ?: ""
        val openingBalance = inputData.getString("openingBalance") ?: ""
        val openingBalanceDate = inputData.getString("openingBalanceDate") ?: ""
        val virtualBalance = inputData.getString("virtualBalance") ?: ""
        val notes = inputData.getString("notes") ?: ""

        genericService?.create(AccountsService::class.java)?.addAccount(name, accountType, currencyCode,
                iBanString, bicString, accountNumber, openingBalance, openingBalanceDate,
                accountRole, virtualBalance, includeNetWorth, notes, liabilityType, liabilityAmount,
                liabilityStartDate, interest, interestPeriod)?.enqueue(
                retrofitCallback({ response ->
                    var errorBody = ""
                    var error = ""
                    if (response.errorBody() != null) {
                        errorBody = String(response.errorBody()?.bytes()!!)
                    }
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    if (response.isSuccessful) {
                        context.displayNotification("$name was added successfully!", "Account Added",
                                Constants.ACCOUNT_CHANNEL, channelIcon)
                        Result.success()
                    } else {
                        error = when {
                            gson.errors.name != null -> gson.errors.name[0]
                            gson.errors.account_number != null -> gson.errors.account_number[0]
                            gson.errors.interest != null -> gson.errors.interest[0]
                            gson.errors.liabilityStartDate != null -> gson.errors.liabilityStartDate[0]
                            else -> "Error saving account"
                        }
                        context.displayNotification(error, "There was an error adding $name",
                                Constants.ACCOUNT_CHANNEL, channelIcon)
                        Result.failure()
                    }
                })
        )

        return Result.success()
    }
}