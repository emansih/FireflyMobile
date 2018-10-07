package xyz.hisname.fireflyiii.repository.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.google.gson.Gson
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.PiggybankService
import xyz.hisname.fireflyiii.repository.models.piggy.ErrorModel
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils
import xyz.hisname.fireflyiii.util.retrofitCallback


class PiggyBankWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private lateinit var success: Result

    override fun doWork(): Result {
        val name = inputData.getString("name") ?: ""
        val accountId = inputData.getString("accountId") ?: ""
        val targetAmount = inputData.getString("targetAmount") ?: ""
        val currentAmount = inputData.getString("currentAmount")
        val startDate = inputData.getString("startDate")
        val endDate = inputData.getString("endDate")
        val notes = inputData.getString("notes")
        val piggyBankService = RetrofitBuilder.getClient(baseUrl, accessToken)?.create(PiggybankService::class.java)
        val notif = NotificationUtils(context)
        piggyBankService?.createNewPiggyBank(name, accountId, targetAmount, currentAmount, startDate, endDate, notes)?.enqueue(retrofitCallback({ response ->
            var errorBody = ""
            if (response.errorBody() != null) {
                errorBody = String(response.errorBody()?.bytes()!!)
            }
            val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
            success = if (response.isSuccessful) {
                notif.showPiggyBankNotification("$name added successfully!", "Piggy Bank Added")
                Result.SUCCESS
            } else {
                var error = ""
                when {
                    gson.errors.name != null -> error = gson.errors.name[0]
                    gson.errors.account_id != null -> error = gson.errors.account_id[0]
                    gson.errors.current_amount != null -> error = gson.errors.current_amount[0]
                }
                notif.showPiggyBankNotification(error, "Error Adding $name")
                Result.FAILURE
            }
        })
        { throwable ->
            notif.showPiggyBankNotification(throwable.message.toString(), "Error adding Piggy Bank")
            success = Result.FAILURE
        })
        return Result.SUCCESS
    }

}