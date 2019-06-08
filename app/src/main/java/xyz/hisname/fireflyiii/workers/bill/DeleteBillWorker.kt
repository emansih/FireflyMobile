package xyz.hisname.fireflyiii.workers.bill

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.*
import retrofit2.Response
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.remote.api.BillsService
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.bills.BillAttributes
import xyz.hisname.fireflyiii.repository.models.bills.BillsModel
import xyz.hisname.fireflyiii.workers.BaseWorker
import xyz.hisname.fireflyiii.ui.notifications.displayNotification

class DeleteBillWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val billDatabase by lazy { AppDatabase.getInstance(context).billDataDao() }
    private val channelIcon = R.drawable.ic_calendar_blank

    override suspend fun doWork(): Result {
        val billId = inputData.getLong("billId", 0)
        var billAttribute: BillAttributes? = null
        var networkResponse: Response<BillsModel>? = null
        runBlocking(Dispatchers.IO) {
            billAttribute = billDatabase.getBillById(billId)[0].billAttributes
            networkResponse = genericService?.create(BillsService::class.java)?.deleteBillById(billId)
        }

        if (networkResponse?.code() == 204 || networkResponse?.code() == 200) {
            context.displayNotification(billAttribute?.name + "successfully deleted", context.getString(R.string.bill),
                    Constants.BILL_CHANNEL, channelIcon)
        } else {
            Result.failure()
            context.displayNotification("There was an issue deleting " + billAttribute?.name, context.getString(R.string.bill),
                    Constants.BILL_CHANNEL, channelIcon)
        }
        return Result.success()
    }


}