package xyz.hisname.fireflyiii

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat.startActivity
import androidx.work.*
import xyz.hisname.fireflyiii.repository.workers.BillWorker
import xyz.hisname.fireflyiii.repository.workers.PiggyBankWorker
import xyz.hisname.fireflyiii.repository.workers.TranscationWorker
import xyz.hisname.fireflyiii.ui.HomeActivity
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils

class GenericReceiver: BroadcastReceiver(){

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra("transaction_notif")
        if(action != null){
            // close the notification tray
            context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
            val intent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            when (action) {
                "expense" -> intent.putExtra("transaction", "expense")
                "income" -> intent.putExtra("transaction", "income")
                "transfer" -> intent.putExtra("transaction", "transfer")
            }
            context.startActivity(intent)
        }

    }
}