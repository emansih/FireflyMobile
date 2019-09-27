package xyz.hisname.fireflyiii.receiver

import android.accounts.AccountManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import androidx.work.*
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.workers.transaction.TransactionWorker
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils

class TransactionReceiver: BroadcastReceiver()  {

    override fun onReceive(context: Context, intent: Intent) {
        if(AppPref(PreferenceManager.getDefaultSharedPreferences(context)).baseUrl.isBlank() ||
                AuthenticatorManager(AccountManager.get(context)).accessToken.isBlank()){
            val notif = NotificationUtils(context)
            notif.showNotSignedIn()
        } else {
            val transactionData = Data.Builder()
                    .putString("description", intent.getStringExtra("description"))
                    .putString("date", intent.getStringExtra("date"))
                    .putString("amount", intent.getStringExtra("amount"))
                    .putString("currency", intent.getStringExtra("currency"))
                    .putString("billName", intent.getStringExtra("billName"))
                    .putString("tags", intent.getStringExtra("tags"))
                    .putString("categoryName", intent.getStringExtra("categoryName"))
                    .putString("budgetName", intent.getStringExtra("budgetName"))
                    .putString("interestDate", intent.getStringExtra("interestDate"))
                    .putString("bookDate", intent.getStringExtra("bookDate"))
                    .putString("processDate", intent.getStringExtra("processDate"))
                    .putString("dueDate", intent.getStringExtra("dueDate"))
                    .putString("paymentDate", intent.getStringExtra("paymentDate"))
                    .putString("invoiceDate", intent.getStringExtra("invoiceDate"))
            when {
                intent.action == "firefly.hisname.ADD_DEPOSIT" -> {
                    val depositData = transactionData.putString("destinationName",
                            intent.getStringExtra("destinationName"))
                            .putString("billName", intent.getStringExtra("billName"))
                    transactionWork(context, depositData, "deposit")
                }
                intent.action == "firefly.hisname.ADD_WITHDRAW" -> {
                    val withdrawData = transactionData.putString("sourceName",
                            intent.getStringExtra("sourceName"))
                    transactionWork(context, withdrawData, "withdrawal")
                }
                intent.action == "firefly.hisname.ADD_TRANSFER" -> {
                    val transferData = transactionData
                            .putString("sourceName", intent.getStringExtra("sourceName"))
                            .putString("destinationName", intent.getStringExtra("destinationName"))
                            .putString("piggyBankName", intent.getStringExtra("piggyBankName"))
                    transactionWork(context, transferData, "transfer")
                }
                else -> {

                }
            }
        }
    }

    private fun transactionWork(context: Context, data: Data.Builder, type: String){
        val transactionWork = OneTimeWorkRequest.Builder(TransactionWorker::class.java)
                .setInputData(data.putString("transactionType" ,type).build())
                .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .build()
        WorkManager.getInstance(context).enqueue(transactionWork)
    }
}