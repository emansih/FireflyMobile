package xyz.hisname.fireflyiii.workers.account

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.remote.api.AccountsService
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.account.AccountRepository
import xyz.hisname.fireflyiii.repository.models.accounts.AccountAttributes
import xyz.hisname.fireflyiii.workers.BaseWorker
import xyz.hisname.fireflyiii.ui.notifications.displayNotification

class DeleteAccountWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val accountDatabase by lazy { AppDatabase.getInstance(context).accountDataDao() }
    private val channelIcon = R.drawable.ic_euro_sign

    companion object {
        fun deleteWorker(accountId: Long){
            val accountTag =
                    WorkManager.getInstance().getWorkInfosByTag("delete_account_$accountId").get()
            if(accountTag == null || accountTag.size == 0) {
                val accountData = Data.Builder()
                        .putLong("id", accountId)
                        .build()
                val deleteAccountWork = OneTimeWorkRequest.Builder(DeleteAccountWorker::class.java)
                        .setInputData(accountData)
                        .addTag("delete_account_$accountId")
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED).build())
                        .build()
                WorkManager.getInstance().enqueue(deleteAccountWork)
            }
        }
    }

    override suspend fun doWork(): Result {
        val accountId = inputData.getLong("accountId", 0L)
        var accountAttributes: AccountAttributes? = null
        var isDeleted = false
        val accountService = genericService?.create(AccountsService::class.java)
        val repository = AccountRepository(accountDatabase, accountService)
        runBlocking(Dispatchers.IO) {
            accountAttributes = repository.retrieveAccountById(accountId)[0].accountAttributes
            isDeleted = repository.deleteAccountById(accountId)
        }
        if(isDeleted){
            Result.success()
            context.displayNotification(accountAttributes?.name + "successfully deleted", context.getString(R.string.account),
                    Constants.ACCOUNT_CHANNEL, channelIcon)
        } else {
            Result.failure()
            context.displayNotification("There was an issue deleting " + accountAttributes?.name, context.getString(R.string.account),
                    Constants.ACCOUNT_CHANNEL, channelIcon)
        }
        return Result.success()
    }


}