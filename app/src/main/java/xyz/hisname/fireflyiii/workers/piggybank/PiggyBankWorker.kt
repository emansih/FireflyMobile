package xyz.hisname.fireflyiii.workers.piggybank

import android.content.Context
import androidx.work.WorkerParameters
import com.google.gson.Gson
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.remote.api.PiggybankService
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.workers.BaseWorker
import xyz.hisname.fireflyiii.ui.notifications.displayNotification
import xyz.hisname.fireflyiii.util.retrofitCallback


class PiggyBankWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val channelName: String = "Piggy Bank"
    private val channelDescription = "Show Piggy Bank Notifications"
    private val channelIcon = R.drawable.ic_sort_descending

    override fun doWork(): Result {
        val name = inputData.getString("name") ?: ""
        val accountId = inputData.getString("accountId") ?: ""
        val targetAmount = inputData.getString("targetAmount") ?: ""
        val currentAmount = inputData.getString("currentAmount")
        val startDate = inputData.getString("startDate")
        val endDate = inputData.getString("endDate")
        val notes = inputData.getString("notes")
        genericService?.create(PiggybankService::class.java)?.createNewPiggyBank(name, accountId, targetAmount, currentAmount, startDate, endDate, notes)?.enqueue(retrofitCallback({ response ->
            var errorBody = ""
            if (response.errorBody() != null) {
                errorBody = String(response.errorBody()?.bytes()!!)
            }
            val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
            if (response.isSuccessful) {
                context.displayNotification("$name was added successfully!", "Piggy Bank Added",
                        Constants.PIGGY_BANK_CHANNEL, channelName, channelDescription, channelIcon)
                Result.SUCCESS
            } else {
                var error = ""
                when {
                    gson.errors.name != null -> error = gson.errors.name[0]
                    gson.errors.account_id != null -> error = gson.errors.account_id[0]
                    gson.errors.current_amount != null -> error = gson.errors.current_amount[0]
                }
                context.displayNotification(error, "There was an issue adding $name",
                        Constants.PIGGY_BANK_CHANNEL, channelName, channelDescription, channelIcon)
                Result.FAILURE
            }
        })
        { throwable ->
            context.displayNotification(throwable.message.toString(), "Error adding $name",
                    Constants.PIGGY_BANK_CHANNEL, channelName, channelDescription, channelIcon)
            Result.FAILURE
        })
        return Result.SUCCESS
    }

}