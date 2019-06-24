package xyz.hisname.fireflyiii.workers.bill

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.bills.BillRepository
import xyz.hisname.fireflyiii.repository.models.bills.BillAttributes
import xyz.hisname.fireflyiii.workers.BaseWorker
import xyz.hisname.fireflyiii.ui.notifications.displayNotification

class DeleteBillWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val billDatabase by lazy { AppDatabase.getInstance(context).billDataDao() }
    private val channelIcon = R.drawable.ic_calendar_blank

    companion object {
        fun initWorker(billId: Long){
            val accountTag =
                    WorkManager.getInstance().getWorkInfosByTag("delete_bill_$billId").get()
            if(accountTag == null || accountTag.size == 0) {
                val accountData = Data.Builder()
                        .putLong("billId", billId)
                        .build()
                val deleteAccountWork = OneTimeWorkRequest.Builder(DeleteBillWorker::class.java)
                        .setInputData(accountData)
                        .addTag("delete_bill_$billId")
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED).build())
                        .build()
                WorkManager.getInstance().enqueue(deleteAccountWork)
            }
        }
    }

    override suspend fun doWork(): Result {
        val billId = inputData.getLong("billId", 0)
        var billAttribute: BillAttributes? = null
        var isDeleted = false
        val billService = genericService?.create(BillsService::class.java)
        val repository = BillRepository(billDatabase, billService)

        runBlocking(Dispatchers.IO) {
            billAttribute = repository.retrieveBillById(billId)[0].billAttributes
            isDeleted = repository.deleteBillById(billId)
        }
        if (isDeleted) {
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