package xyz.hisname.fireflyiii.receiver

import android.accounts.AccountManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils
import xyz.hisname.fireflyiii.workers.account.AccountWorker

class AccountReceiver: BroadcastReceiver()  {

    override fun onReceive(context: Context, intent: Intent) {
        if(AppPref(PreferenceManager.getDefaultSharedPreferences(context)).baseUrl.isBlank() ||
                AuthenticatorManager(AccountManager.get(context)).accessToken.isBlank()){
            val notif = NotificationUtils(context)
            notif.showNotSignedIn()
        } else {
            if(intent.action == "firefly.hisname.ADD_ACCOUNT"){
                AccountWorker.initWorker(context, intent)
            }
        }
    }
}