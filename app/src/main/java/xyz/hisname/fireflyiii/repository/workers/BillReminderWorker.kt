package xyz.hisname.fireflyiii.repository.workers

import android.content.Context
import androidx.work.WorkerParameters
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils
import xyz.hisname.fireflyiii.util.DateTimeUtil

class BillReminderWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {


    override fun doWork(): Result {
        val name = inputData.getString("name")
        val date = inputData.getString("date")
        val dateDiff = DateTimeUtil.getDaysDifference(date)
        val notif = NotificationUtils(context)
        notif.showBillDueDate("$name is due on $date.($dateDiff days from now)", "Bill Due Reminder")
        return Result.SUCCESS
    }


}