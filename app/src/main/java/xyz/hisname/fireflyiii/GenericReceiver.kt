package xyz.hisname.fireflyiii

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import xyz.hisname.fireflyiii.ui.HomeActivity

class GenericReceiver: BroadcastReceiver(){

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra("transaction_notif")
        if(action != null){
            // close the notification tray
            context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
            val trans = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            when (action) {
                "Deposit" -> trans.putExtra("transaction", "Deposit")
                "Withdrawal" -> trans.putExtra("transaction", "Withdrawal")
                "Transfer" -> trans.putExtra("transaction", "Transfer")
            }
            context.startActivity(trans)
        }

    }
}