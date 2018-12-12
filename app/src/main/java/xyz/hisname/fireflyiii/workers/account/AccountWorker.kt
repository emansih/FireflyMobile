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

    private val channelName = "Account"
    private val channelDescription = "Show Account Notifications"
    private val channelIcon = R.drawable.ic_euro_sign

    override fun doWork(): Result {
        val name = inputData.getString("name") ?: ""
        val type = inputData.getString("type") ?: ""
        val currencyCode = inputData.getString("currencyCode") ?: ""
        val includeNetWorth = inputData.getInt("includeNetWorth",0)
        val accountRole = inputData.getString("accountRole")
        val ccType = inputData.getString("ccType")
        val ccMonthlyPaymentDate = inputData.getString("ccMonthlyPaymentDate")
        val liabilityType = inputData.getString("liabilityType")
        val liabilityAmount = inputData.getString("liabilityAmount")
        val liabilityStartDate = inputData.getString("liabilityStartDate")
        val interest = inputData.getString("interest")
        val interestPeriod = inputData.getString("interestPeriod")
        val accountNumber = inputData.getString("accountNumber")
        genericService?.create(AccountsService::class.java)?.addAccount(name,type,currencyCode,1,includeNetWorth,accountRole,ccType,
                ccMonthlyPaymentDate,liabilityType,liabilityAmount,liabilityStartDate,interest,interestPeriod,accountNumber)?.enqueue(
                retrofitCallback({ response ->
                    var errorBody = ""
                    var error = ""
                    if (response.errorBody() != null) {
                        errorBody = String(response.errorBody()?.bytes()!!)
                    }
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    if (response.isSuccessful) {
                        context.displayNotification("$name was added successfully!", "Account Added",
                                Constants.ACCOUNT_CHANNEL, channelName, channelDescription, channelIcon)
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
                                Constants.ACCOUNT_CHANNEL, channelName, channelDescription, channelIcon)
                        Result.failure()
                    }
                })
        )

        return Result.success()
    }
}