package xyz.hisname.fireflyiii.workers.bill

import android.content.Context
import androidx.work.*
import com.google.gson.Gson
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.remote.api.BillsService
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.workers.BaseWorker
import xyz.hisname.fireflyiii.ui.notifications.displayNotification
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class BillWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val channelName = "Bill"
    private val channelDescription = "Show Bill Notifications"
    private val channelIcon = R.drawable.ic_calendar_blank

    override fun doWork(): Result {
        val name = inputData.getString("name") ?: ""
        val billMatch = inputData.getString("billMatch") ?: ""
        val minAmount = inputData.getString("minAmount") ?: ""
        val maxAmount = inputData.getString("maxAmount") ?: ""
        val billDate = inputData.getString("billDate") ?: ""
        val repeatFreq = inputData.getString("repeatFreq") ?: ""
        val skip = inputData.getString("skip") ?: ""
        val currencyCode = inputData.getString("currencyCode") ?: ""
        val notes = inputData.getString("notes")
        genericService?.create(BillsService::class.java)?.createBill(name, billMatch, minAmount,
                maxAmount, billDate, repeatFreq, skip, "1","1", currencyCode, notes)?.enqueue(
                retrofitCallback({ response ->
                    var errorBody = ""
                    var error = ""
                    if (response.errorBody() != null) {
                        errorBody = String(response.errorBody()?.bytes()!!)
                    }
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    if (response.isSuccessful) {
                        context.displayNotification("$name added successfully!", "Bill Added",
                                Constants.BILL_CHANNEL, channelName, channelDescription, channelIcon)
                        Result.success()
                    } else {
                        when {
                            gson.errors.name != null -> error = gson.errors.name[0]
                            gson.errors.currency_code != null -> error = gson.errors.currency_code[0]
                            gson.errors.amount_min != null -> error = gson.errors.amount_min[0]
                            gson.errors.repeat_freq != null -> error = gson.errors.repeat_freq[0]
                            gson.errors.automatch != null -> error = gson.errors.automatch[0]
                        }
                        context.displayNotification(error, "Error Adding $name",
                                Constants.BILL_CHANNEL, channelName, channelDescription, channelIcon)
                        Result.failure()
                    }
                })
                { throwable ->
                    context.displayNotification(throwable.message.toString(), "Error Adding $name",
                            Constants.BILL_CHANNEL, channelName, channelDescription, channelIcon)
                    Result.failure()
                })
        return Result.success()
    }
}