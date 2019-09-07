package xyz.hisname.fireflyiii.data.local.pref

import android.content.SharedPreferences
import androidx.core.content.edit
import xyz.hisname.fireflyiii.util.extension.getDouble
import xyz.hisname.fireflyiii.util.extension.putDouble

class SimpleData(private val sharedPref: SharedPreferences) {

    var networthValue
        get() = sharedPref.getDouble("networthValue", 0.0)
        set(value) = sharedPref.edit { putDouble("networthValue", value) }

    var leftToSpend
        get() = sharedPref.getDouble("leftToSpend", 0.0)
        set(value) = sharedPref.edit { putDouble("leftToSpend", value) }

    var balance
        get() = sharedPref.getDouble("balance", 0.0)
        set(value) = sharedPref.edit { putDouble("balance", value) }

}