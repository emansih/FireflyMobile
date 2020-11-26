package xyz.hisname.fireflyiii.data.local.pref

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.work.NetworkType

class AppPref(private val sharedPref: SharedPreferences): PreferenceHelper {

    override var baseUrl
        get() = sharedPref.getString("fireflyUrl", "") ?: ""
        set(value) = sharedPref.edit { putString("fireflyUrl", value) }

    override var isTransactionPersistent
        get() = sharedPref.getBoolean("persistent_notification", false)
        set(value) = sharedPref.edit { putBoolean("persistent_notification", value) }

    override var userRole
        get() = sharedPref.getString("userRole", "") ?: ""
        set(value) = sharedPref.edit { putString("userRole", value) }

    override var remoteApiVersion
        get() = sharedPref.getString("api_version", "") ?: ""
        set(value) = sharedPref.edit { putString("api_version", value) }

    override var serverVersion
        get() = sharedPref.getString("server_version", "") ?: ""
        set(value) = sharedPref.edit { putString("server_version", value) }

    override var userOs
        get() = sharedPref.getString("user_os", "") ?: ""
        set(value) = sharedPref.edit { putString("user_os", value) }

    override var certValue
        get() = sharedPref.getString("cert_value", "") ?: ""
        set(value) = sharedPref.edit { putString("cert_value", value) }

    override var languagePref: String
        get() = sharedPref.getString("language_pref", "") ?: "en"
        set(value) = sharedPref.edit{ putString("language_pref", value)}

    override var nightModeEnabled: Boolean
        get() = sharedPref.getBoolean("night_mode", false)
        set(value) = sharedPref.edit { putBoolean("night_mode", value) }

    override var isKeyguardEnabled: Boolean
        get() = sharedPref.getBoolean("keyguard", false)
        set(value) = sharedPref.edit{ putBoolean("keyguard", value)}

    override var timeFormat: Boolean
        get() = sharedPref.getBoolean("timeFormat", false)
        set(value) = sharedPref.edit{ putBoolean("timeFormat", value)}

    override var isCustomCa: Boolean
        get() = sharedPref.getBoolean("customCa", false)
        set(value) = sharedPref.edit{ putBoolean("customCa", value)}

    override var isCurrencyThumbnailEnabled: Boolean
        get() = sharedPref.getBoolean("currencyThumbnail", false)
        set(value) = sharedPref.edit{ putBoolean("currencyThumbnail", value) }

    override var workManagerDelay: Long
        get() {
            var delay = sharedPref.getLong("workManagerDelay", 15)
            delay = if(delay < 15){
                15
            } else {
                sharedPref.getLong("workManagerDelay", 15)
            }
            return delay
        }
        set(value) = sharedPref.edit{ putLong("workManagerDelay", value) }

    // Hack so that EditTextPreference don't crash
    var workManagerDelayPref: String
    get() {
        var delay = sharedPref.getLong("workManagerDelay", 15)
        delay = if(delay < 15){
            15
        } else {
            sharedPref.getLong("workManagerDelay", 15)
        }
        return delay.toString()
    }
    set(value) {
        sharedPref.edit{ putLong("workManagerDelay", java.lang.Long.parseLong(value)) }
        sharedPref.edit{ putString("workManagerDelayPref", value) }
    }

    override var workManagerLowBattery: Boolean
        get() = sharedPref.getBoolean("workManagerLowBattery", true)
        set(value) = sharedPref.edit{ putBoolean("workManagerLowBattery", value) }

    override var workManagerNetworkType: NetworkType
        get() = toNetworkType(sharedPref.getString("workManagerType", NetworkType.CONNECTED.toString()) ?: "")
        set(value) = sharedPref.edit{
            putString("workManagerType", value.toString())
        }

    override var workManagerRequireCharging: Boolean
        get() = sharedPref.getBoolean("workManagerCharging", false)
        set(value) = sharedPref.edit{ putBoolean("workManagerCharging", value) }

    private fun toNetworkType(networkType: String): NetworkType {
        return try {
            enumValueOf(networkType)
        } catch (ex: Exception) {
            NetworkType.CONNECTED
        }
    }

    override fun clearPref() = sharedPref.edit().clear().apply()
}