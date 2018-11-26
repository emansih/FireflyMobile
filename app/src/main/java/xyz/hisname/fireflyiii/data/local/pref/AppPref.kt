package xyz.hisname.fireflyiii.data.local.pref

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import androidx.core.os.bundleOf
import java.util.concurrent.TimeUnit

class AppPref(context: Context): PreferenceHelper {

    private val sharedPref: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
    private val account by lazy { Account("Firefly III Mobile", "OAUTH") }
    private val accountManager by lazy { AccountManager.get(context) }

    override var baseUrl
        get() = sharedPref.getString("fireflyUrl", "") ?: ""
        set(value) = sharedPref.edit { putString("fireflyUrl", value) }

    override var secretKey
        get() = accountManager.getUserData(account, "SECRET_KEY") ?: ""
        set(value) {
            accountManager.setUserData(account, "SECRET_KEY", value)
        }

    override var accessToken
        get() = accountManager.getUserData(account, "ACCESS_TOKEN") ?: ""
        set(value) {
            accountManager.setUserData(account, "ACCESS_TOKEN", value)
        }

    override var clientId
        get() = accountManager.getUserData(account, "CLIENT_ID") ?: ""
        set(value) {
            accountManager.setUserData(account, "CLIENT_ID", value)
        }


    override var refreshToken
        get() = accountManager.getUserData(account, "REFRESH_TOKEN") ?: ""
        set(value) {
            accountManager.setUserData(account, "REFRESH_TOKEN", value)
        }

    override var authMethod
        get() = sharedPref.getString("auth_method", "") ?: ""
        set(value) = sharedPref.edit { putString("auth_method", value) }

    override var isTransactionPersistent
        get() = sharedPref.getBoolean("persistent_notification", false)
        set(value) = sharedPref.edit { putBoolean("persistent_notification", value) }

    override var tokenExpiry
        get() = sharedPref.getLong("token_expires_in", 0L)
        set(value) {
            sharedPref.edit {
                putLong("token_expires_in", (System.currentTimeMillis() +
                        TimeUnit.MINUTES.toMillis(value)))
            }
        }

    override var userEmail
        get() = accountManager.getUserData(account, "USER_EMAIL") ?: ""
        set(value) {
            accountManager.setUserData(account, "USER_EMAIL", value)
        }


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

    override var enableCertPinning
        get() = sharedPref.getBoolean("enable_cert_pinning", false)
        set(value) = sharedPref.edit{ putBoolean("enable_cert_pinning", value)}

    override fun clearPref() = sharedPref.edit().clear().apply()
}