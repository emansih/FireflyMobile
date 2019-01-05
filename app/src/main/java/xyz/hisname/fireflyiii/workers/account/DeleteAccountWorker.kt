package xyz.hisname.fireflyiii.workers.account

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.remote.api.AccountsService
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.accounts.AccountAttributes
import xyz.hisname.fireflyiii.workers.BaseWorker
import xyz.hisname.fireflyiii.ui.notifications.displayNotification
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class DeleteAccountWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val accountDatabase by lazy { AppDatabase.getInstance(context).accountDataDao() }
    private val channelName: String = "Account"
    private val channelIcon = R.drawable.ic_euro_sign


    override fun doWork(): Result {
        val id = inputData.getLong("accountId", 0L)
        var accountAttributes: AccountAttributes? = null
        GlobalScope.launch(Dispatchers.Main) {
            val result = async(Dispatchers.IO) {
                accountDatabase.getAccountById(id)
            }.await()
            accountAttributes = result[0].accountAttributes
        }
        genericService?.create(AccountsService::class.java)?.deleteAccountById(id)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                GlobalScope.launch(Dispatchers.Main) {
                    async(Dispatchers.IO) {
                        accountDatabase.deleteAccountById(id)
                    }.await()
                    Result.success()
                    context.displayNotification(accountAttributes?.name + "successfully deleted", context.getString(R.string.account),
                            Constants.ACCOUNT_CHANNEL, channelName, Constants.ACCOUNT_CHANNEL_DESCRIPTION, channelIcon)
                }
            } else {
                Result.failure()
                context.displayNotification("There was an issue deleting " + accountAttributes?.name, context.getString(R.string.account),
                        Constants.ACCOUNT_CHANNEL, channelName, Constants.ACCOUNT_CHANNEL_DESCRIPTION, channelIcon)
            }
        })
        { throwable ->
            Result.failure()
            context.displayNotification(throwable.localizedMessage, context.getString(R.string.account),
                    Constants.ACCOUNT_CHANNEL, channelName, Constants.ACCOUNT_CHANNEL_DESCRIPTION, channelIcon)
        })

        return Result.success()
    }


}