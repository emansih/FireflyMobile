package xyz.hisname.fireflyiii.receiver

import android.accounts.AccountManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import androidx.work.*
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.workers.bill.BillWorker
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils

class BillReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if(AppPref(PreferenceManager.getDefaultSharedPreferences(context)).baseUrl.isBlank() ||
                AuthenticatorManager(AccountManager.get(context)).accessToken.isBlank()){
            val notif = NotificationUtils(context)
            notif.showNotSignedIn()
        } else {
            if(intent.action == "firefly.hisname.ADD_BILL"){
                val billData = Data.Builder()
                        .putString("name", intent.getStringExtra("name"))
                        .putString("minAmount", intent.getStringExtra("minAmount"))
                        .putString("maxAmount", intent.getStringExtra("maxAmount"))
                        .putString("billDate", intent.getStringExtra("billDate"))
                        .putString("repeatFreq", intent.getStringExtra("repeatFreq"))
                        .putString("skip", intent.getStringExtra("skip"))
                        .putString("currencyCode", intent.getStringExtra("currencyCode"))
                        .putString("notes", intent.getStringExtra("notes"))
                        .build()
                val billWork = OneTimeWorkRequest.Builder(BillWorker::class.java)
                        .setInputData(billData)
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build())
                        .build()
                WorkManager.getInstance().enqueue(billWork)
            } else {
                // Invalid intent
            }
        }
    }
}