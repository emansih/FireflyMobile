package xyz.hisname.fireflyiii.workers.bill

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.remote.api.BillsService
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.bills.BillAttributes
import xyz.hisname.fireflyiii.workers.BaseWorker
import xyz.hisname.fireflyiii.ui.notifications.displayNotification
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class DeleteBillWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val billDatabase by lazy { AppDatabase.getInstance(context).billDataDao() }
    private val channelName = "Bill"
    private val channelDescription = "Show Bill Notifications"
    private val channelIcon = R.drawable.ic_calendar_blank

    override fun doWork(): Result {
        val billId = inputData.getLong("billId", 0)
        var billAttribute: BillAttributes? = null
        GlobalScope.launch(context = Dispatchers.Main) {
            val result = async(Dispatchers.IO) {
                billDatabase.getBillById(billId)
            }.await()
            billAttribute  = result[0].billAttributes
        }
        genericService?.create(BillsService::class.java)?.deleteBillById(billId)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                GlobalScope.launch(Dispatchers.Main) {
                    async(Dispatchers.IO) {
                        billDatabase.deleteBillById(billId)
                    }.await()
                    Result.success()
                    context.displayNotification(billAttribute?.name + "successfully deleted", "Bill",
                            Constants.BILL_CHANNEL, channelName, channelDescription, channelIcon)
                }
            } else {
                Result.failure()
                context.displayNotification("There was an issue deleting " + billAttribute?.name, "Bill",
                        Constants.BILL_CHANNEL, channelName, channelDescription, channelIcon)
            }
        })
        { throwable ->
            Result.failure()
            context.displayNotification(throwable.localizedMessage, "Bill",
                    Constants.BILL_CHANNEL, channelName, channelDescription, channelIcon)
        })

        return Result.success()
    }


}