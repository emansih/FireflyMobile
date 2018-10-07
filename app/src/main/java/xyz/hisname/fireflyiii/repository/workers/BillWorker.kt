package xyz.hisname.fireflyiii.repository.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.google.gson.Gson
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.BillsService
import xyz.hisname.fireflyiii.repository.models.bills.ErrorModel
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils
import xyz.hisname.fireflyiii.util.retrofitCallback

class BillWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    /*

            model.addBill(baseUrl,accessToken,bill_name_edittext.getString(),
                        bill_match_edittext.getString(),amount_min_edittext.getString(),
                        amount_max_edittext.getString(), bill_date_edittext.getString(), repeatFreq,
                        skip_edittext.getString(), "1", "1",
                        currency_code_edittext.getString(), notes)

     */
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
        val notif = NotificationUtils(context)
        val billsService = RetrofitBuilder.getClient(baseUrl, accessToken)?.create(BillsService::class.java)
        billsService?.createBill(name, billMatch, minAmount, maxAmount, billDate, repeatFreq, skip,
                "1","1", currencyCode, notes)?.enqueue(retrofitCallback({ response ->
            var errorBody = ""
            var error = ""
            if (response.errorBody() != null) {
                errorBody = String(response.errorBody()?.bytes()!!)
            }
            val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
            if(response.isSuccessful){
                notif.showBillNotification("$name added successfully!", "Bill Added")
                Result.SUCCESS
            } else {
                when {
                    gson.errors.name != null -> error = gson.errors.name[0]
                    gson.errors.currency_code != null -> error = gson.errors.currency_code[0]
                    gson.errors.amount_min != null -> error = gson.errors.amount_min[0]
                    gson.errors.repeat_freq != null -> error = gson.errors.repeat_freq[0]
                    gson.errors.automatch != null -> error = gson.errors.automatch[0]
                }
                notif.showBillNotification(error, "Error Adding $name")
                Result.FAILURE
            }
        })
        { throwable ->
            notif.showBillNotification(throwable.message.toString(), "Error adding Piggy Bank")
            Result.FAILURE
        })
        return Result.SUCCESS
    }
}