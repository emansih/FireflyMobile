package xyz.hisname.fireflyiii.data.local.pref

interface PreferenceHelper {

    var baseUrl: String
    var secretKey: String
    var accessToken: String
    var clientId: String
    var refreshToken: String
    var tokenExpiry: Long
    var authMethod: String
    var isTransactionPersistent: Boolean
    var userEmail: String
    var userRole: String
    var remoteApiVersion: String
    var serverVersion: String
    var userOs: String
    var enableCertPinning: Boolean
    var certValue: String
    fun clearPref()
}