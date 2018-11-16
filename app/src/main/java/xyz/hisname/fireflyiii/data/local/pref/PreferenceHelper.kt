package xyz.hisname.fireflyiii.data.local.pref

interface PreferenceHelper {

    fun getBaseUrl(): String
    fun setBaseUrl(url: String)
    fun getSecretKey(): String
    fun setSecretKey(key: String)
    fun getAccessToken(): String
    fun setAccessToken(token: String)
    fun getClientId(): String
    fun setClientId(id: String)
    fun getRefreshToken(): String
    fun setRefreshToken(refreshToken: String)
    fun getTokenExpiry(): Long
    fun setTokenExpiry(time: Long)
    fun getAuthMethod(): String
    fun setAuthMethod(method: String)
    fun isTransactionPersistent(): Boolean
    fun setTransactionPersistent(yesno: Boolean)
    fun getUserEmail(): String
    fun setUserEmail(email: String)
    fun getUserRole(): String
    fun setUserRole(role: String)
    fun getRemoteApiVersion(): String
    fun setRemoteApiVersion(apiVersion: String)
    fun getServerVersion(): String
    fun setServerVersion(version: String)
    fun getUserOS(): String
    fun setUserOS(os: String)
    fun clearPref()
}