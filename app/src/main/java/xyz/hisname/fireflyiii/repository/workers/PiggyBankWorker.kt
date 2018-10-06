package xyz.hisname.fireflyiii.repository.workers

import android.content.Context
import androidx.work.WorkerParameters
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.PiggybankService
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils
import xyz.hisname.fireflyiii.util.retrofitCallback


class PiggyBankWorker(context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private lateinit var success: Result

    override fun doWork(): Result = try {
        val name = inputData.getString("name") ?: ""
        val accountId = inputData.getString("accountId") ?: ""
        val targetAmount = inputData.getString("targetAmount") ?: ""
        val currentAmount = inputData.getString("currentAmount")
        val startDate = inputData.getString("startDate")
        val endDate = inputData.getString("endDate")
        val notes = inputData.getString("notes")
        val piggyBankService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(PiggybankService::class.java)
        val notif = NotificationUtils(applicationContext)
        piggyBankService?.createNewPiggyBank(name,accountId,targetAmount,currentAmount,startDate,endDate, notes)?.enqueue(retrofitCallback({ response ->
            success = if(response.isSuccessful){
                notif.showPiggyBankNotification("Piggy bank added successfully!")
                Result.SUCCESS
            } else {
                notif.showPiggyBankNotification("There was an issue adding your piggy bank")
                Result.FAILURE
            }
        })
        { _ ->
            notif.showPiggyBankNotification("There was an issue adding your piggy bank")
            success = Result.FAILURE
        })
        success
    } catch (e: Throwable) {
        val notif = NotificationUtils(applicationContext)
        notif.showPiggyBankNotification("There was an issue adding your piggy bank")
        Result.FAILURE
    }
}