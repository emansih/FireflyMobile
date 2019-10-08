package xyz.hisname.fireflyiii.data.local.pref

import android.content.SharedPreferences
import androidx.core.content.edit

class SimpleData(private val sharedPref: SharedPreferences) {

    var networthValue
        get() = sharedPref.getString("networthValue", "")
        set(value) = sharedPref.edit { putString("networthValue", value) }

    var leftToSpend
        get() = sharedPref.getString("leftToSpend", "")
        set(value) = sharedPref.edit { putString("leftToSpend", value) }

    var balance
        get() = sharedPref.getString("balance", "")
        set(value) = sharedPref.edit { putString("balance", value) }

    var earned
        get() = sharedPref.getString("earned", "")
        set(value) = sharedPref.edit { putString("earned", value) }

    var spent
        get() = sharedPref.getString("spent", "")
        set(value) = sharedPref.edit { putString("spent", value) }

    var unPaidBills
        get() = sharedPref.getString("unpaidBills", "")
        set(value) = sharedPref.edit { putString("unpaidBills", value) }

    var paidBills
        get() = sharedPref.getString("paidBills", "")
        set(value) = sharedPref.edit { putString("paidBills", value) }

    var leftToSpendPerDay
        get() = sharedPref.getString("leftToSpendPerDay", "")
        set(value) = sharedPref.edit { putString("leftToSpendPerDay", value) }
}