package xyz.hisname.fireflyiii.repository.workers.account

import android.content.Context
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.api.AccountsService
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.accounts.AccountAttributes
import xyz.hisname.fireflyiii.repository.workers.BaseWorker
import xyz.hisname.fireflyiii.ui.notifications.displayNotification
import xyz.hisname.fireflyiii.util.retrofitCallback

class DeleteAccountWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val accountDatabase by lazy { AppDatabase.getInstance(context)?.accountDataDao() }
    private val channelName: String = "Account"
    private val channelDescription = "Show Account Notifications"
    private val channelIcon = R.drawable.ic_euro_sign


    override fun doWork(): Result {
        val id = inputData.getString("accountId") ?: ""
        var accountAttributes: AccountAttributes? = null
        GlobalScope.launch(Dispatchers.Main) {
            val result = async(Dispatchers.IO) {
                accountDatabase?.getAccountById(id.toLong())
            }.await()
            accountAttributes = result!![0].accountAttributes
        }
        genericService?.create(AccountsService::class.java)?.deleteAccountById(id)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                GlobalScope.launch(Dispatchers.Main) {
                    async(Dispatchers.IO) {
                        accountDatabase?.deleteAccountById(id.toLong())
                    }.await()
                    context.displayNotification(accountAttributes?.name + "successfully deleted", "Account",
                            Constants.ACCOUNT_CHANNEL, channelName, channelDescription, channelIcon)
                }
            } else {
                context.displayNotification("There was an issue deleting " + accountAttributes?.name , "Account",
                        Constants.ACCOUNT_CHANNEL, channelName, channelDescription, channelIcon)
            }
        })
        { throwable ->
            context.displayNotification(throwable.localizedMessage, "Account",
                    Constants.ACCOUNT_CHANNEL, channelName, channelDescription, channelIcon)
        })

        return Result.SUCCESS
    }


}