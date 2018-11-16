package xyz.hisname.fireflyiii.data.local.pref

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import java.util.concurrent.TimeUnit

class AppPref(context: Context): PreferenceHelper {

    private val sharedPref: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    override fun getBaseUrl() = sharedPref.getString("fireflyUrl", "") ?: ""

    override fun setBaseUrl(url: String) = sharedPref.edit{ putString("fireflyUrl", url) }

    override fun getSecretKey() = sharedPref.getString("fireflySecretKey", "") ?: ""

    override fun setSecretKey(key: String) = sharedPref.edit { putString("fireflySecretKey", key) }

    override fun getClientId() = sharedPref.getString("fireflyId", "") ?: ""

    override fun setClientId(id: String) = sharedPref.edit{ putString("fireflyId", id)}

    override fun getAccessToken() = sharedPref.getString("access_token", "") ?: ""

    override fun setAccessToken(token: String) =  sharedPref.edit { putString("access_token", token) }

    override fun getRefreshToken() = sharedPref.getString("refresh_token", "") ?: ""

    override fun setRefreshToken(refreshToken: String) = sharedPref.edit{ putString("refresh_token", refreshToken)}

    override fun getAuthMethod() = sharedPref.getString("auth_method", "") ?: ""

    override fun setAuthMethod(method: String) = sharedPref.edit { putString("auth_method", method) }

    override fun isTransactionPersistent(): Boolean = sharedPref.getBoolean("persistent_notification", false)

    override fun setTransactionPersistent(yesno: Boolean) = sharedPref.edit { putBoolean("persistent_notification", yesno) }

    override fun getTokenExpiry(): Long = sharedPref.getLong("expires_at", 0L)

    override fun setTokenExpiry(time: Long) = sharedPref.edit {
        putLong("expires_at", (System.currentTimeMillis() +
                    TimeUnit.MINUTES.toMillis(time)))
    }

    override fun getUserEmail() = sharedPref.getString("userEmail", "") ?: ""

    override fun setUserEmail(email: String) = sharedPref.edit { putString("userEmail", email) }

    override fun getUserRole() = sharedPref.getString("userRole", "") ?: ""

    override fun setUserRole(role: String) = sharedPref.edit { putString("userRole", role) }

    override fun getRemoteApiVersion() = sharedPref.getString("api_version", "") ?: ""

    override fun setRemoteApiVersion(apiVersion: String) = sharedPref.edit{ putString("api_version", apiVersion) }

    override fun getServerVersion() = sharedPref.getString("server_version", "") ?: ""

    override fun setServerVersion(version: String) = sharedPref.edit { putString("server_version", version) }

    override fun getUserOS() = sharedPref.getString("user_os", "") ?: ""

    override fun setUserOS(os: String) = sharedPref.edit { putString("user_os", os) }

    override fun clearPref() = sharedPref.edit().clear().apply()
}