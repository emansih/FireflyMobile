package xyz.hisname.fireflyiii.data.local.pref

import android.content.SharedPreferences
import androidx.core.content.edit

class SimpleData(private val sharedPref: SharedPreferences) {

    var networthValue
        get() = sharedPref.getString("networthValue", "") ?: ""
        set(value) = sharedPref.edit { putString("networthValue", value) }

    var leftToSpend
        get() = sharedPref.getString("leftToSpend", "") ?: ""
        set(value) = sharedPref.edit { putString("leftToSpend", value) }

}