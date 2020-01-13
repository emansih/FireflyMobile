package xyz.hisname.fireflyiii.util.biometric

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import androidx.preference.PreferenceManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref

class KeyguardUtil(private val activity: Activity) {

    fun isDeviceKeyguardEnabled(): Boolean{
        val keyguardManager  = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isKeyguardSecure
    }

    fun isAppKeyguardEnabled(): Boolean{
        return AppPref(PreferenceManager.getDefaultSharedPreferences(activity)).isKeyguardEnabled
    }

}