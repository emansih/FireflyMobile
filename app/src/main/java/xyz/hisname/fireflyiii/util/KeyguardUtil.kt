package xyz.hisname.fireflyiii.util

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import androidx.preference.PreferenceManager
import androidx.annotation.RequiresApi
import xyz.hisname.fireflyiii.data.local.pref.AppPref

class KeyguardUtil(private val activity: Activity) {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun initKeyguard(){
        val keyguardManager  = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val screenLockIntent = keyguardManager.createConfirmDeviceCredentialIntent("", "")
        activity.startActivityForResult(screenLockIntent, 2804)
    }


    fun isDeviceKeyguardEnabled(): Boolean{
        val keyguardManager  = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isKeyguardSecure
    }

    fun isAppKeyguardEnabled(): Boolean{
        return AppPref(PreferenceManager.getDefaultSharedPreferences(activity)).isKeyguardEnabled
    }

}