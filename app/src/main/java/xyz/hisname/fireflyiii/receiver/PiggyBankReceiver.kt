package xyz.hisname.fireflyiii.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.workers.piggybank.PiggyBankWorker
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils

class PiggyBankReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if(AppPref(context).baseUrl.isBlank() || AppPref(context).accessToken.isBlank()){
            val notif = NotificationUtils(context)
            notif.showNotSignedIn()
        } else {
            if(intent.action == "firefly.hisname.ADD_PIGGY_BANK"){
                val piggyData = Data.Builder()
                        .putString("name", intent.getStringExtra("name"))
                        .putString("accountId", intent.getStringExtra("accountId"))
                        .putString("targetAmount", intent.getStringExtra("targetAmount"))
                        .putString("currentAmount", intent.getStringExtra("currentAmount"))
                        .putString("startDate", intent.getStringExtra("startDate"))
                        .putString("endDate", intent.getStringExtra("endDate"))
                        .putString("notes", intent.getStringExtra("notes"))
                        .build()
                val piggybankWork = OneTimeWorkRequest.Builder(PiggyBankWorker::class.java)
                        .setInputData(piggyData)
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build())
                        .build()
                WorkManager.getInstance().enqueue(piggybankWork)
            } else {
               // Invalid Intent
            }
        }
    }
}