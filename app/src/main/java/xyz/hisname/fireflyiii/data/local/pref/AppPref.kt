package xyz.hisname.fireflyiii.data.local.pref

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import java.util.concurrent.TimeUnit

class AppPref(context: Context): PreferenceHelper {

    private val sharedPref: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    override var baseUrl
        get() = sharedPref.getString("fireflyUrl", "") ?: ""
        set(value) = sharedPref.edit { putString("fireflyUrl", value) }


    override var secretKey
        get() = sharedPref.getString("fireflySecretKey", "") ?: ""
        set(value) = sharedPref.edit { putString("fireflySecretKey", value) }

    override var accessToken
        get() = sharedPref.getString("access_token", "") ?: ""
        set(value) = sharedPref.edit { putString("access_token", value) }

    override var clientId
        get() = sharedPref.getString("fireflyId", "") ?: ""
        set(value) = sharedPref.edit { putString("fireflyId", value) }

    override var refreshToken
        get() = sharedPref.getString("refresh_token", "") ?: ""
        set(value) = sharedPref.edit { putString("refresh_token", value) }

    override var authMethod
        get() = sharedPref.getString("auth_method", "") ?: ""
        set(value) = sharedPref.edit { putString("auth_method", value) }

    override var isTransactionPersistent
        get() = sharedPref.getBoolean("persistent_notification", false)
        set(value) = sharedPref.edit { putBoolean("persistent_notification", value) }

    override var tokenExpiry
        get() = sharedPref.getLong("expires_at", 0L)
        set(value) {
            sharedPref.edit {
                putLong("expires_at", (System.currentTimeMillis() +
                        TimeUnit.MINUTES.toMillis(value)))
            }
        }

    override var userEmail
        get() = sharedPref.getString("userEmail", "") ?: ""
        set(value) = sharedPref.edit { putString("userEmail", value) }


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

    override fun clearPref() = sharedPref.edit().clear().apply()
}