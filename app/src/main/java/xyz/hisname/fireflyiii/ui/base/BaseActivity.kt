package xyz.hisname.fireflyiii.ui.base

import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity: AppCompatActivity() {
    private val sharedPref: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    val baseUrl: String by lazy { sharedPref.getString("fireflyUrl", "") }
    val accessToken: String by lazy { sharedPref.getString("access_token","") }

}