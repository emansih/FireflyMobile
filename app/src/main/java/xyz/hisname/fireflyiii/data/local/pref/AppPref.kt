/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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

    private fun toNetworkType(networkType: String): NetworkType {
        return try {
            enumValueOf(networkType)
        } catch (ex: Exception) {
            NetworkType.CONNECTED
        }
    }

    override var workManagerRequireCharging: Boolean
        get() = sharedPref.getBoolean("workManagerCharging", false)
        set(value) = sharedPref.edit{ putBoolean("workManagerCharging", value) }

    override var budgetIssue4394: Boolean
        get() = sharedPref.getBoolean("budgetIssue4394", false)
        set(value) = sharedPref.edit{ putBoolean("budgetIssue4394", value) }


    /* 0 -> dd MM yyyy hh:mm a   (08:30am)
     * 1 -> dd MM yyyy HH:mm (15:30)
     * 2 -> MM dd yyyy hh:mm a
     * 3 -> MM dd yyyy HH:mm
     * 4 -> dd MMM yyyy hh:mm a
     * 5 -> dd MMM yyyy HH:mm
     * 6 -> MMM dd yyyy hh:mm a
     * 7 -> MMM dd yyyy HH:mm
     */
    override var dateTimeFormat: Int
        get() = (sharedPref.getString("dateTimeFormat", "") ?: "0").toInt()
        set(value) = sharedPref.edit{ putInt("dateTimeFormat", value) }

    override var userDefinedDateTimeFormat: String
        get() = sharedPref.getString("userDefinedDateTimeFormat", "") ?: ""
        set(value) = sharedPref.edit{ putString("userDefinedDateTimeFormat", value) }

    override fun clearPref() = sharedPref.edit().clear().apply()
}