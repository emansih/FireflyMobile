package xyz.hisname.fireflyiii.data.local.pref

interface PreferenceHelper {

    var baseUrl: String
    var isTransactionPersistent: Boolean
    var userRole: String
    var remoteApiVersion: String
    var serverVersion: String
    var userOs: String
    var enableCertPinning: Boolean
    var certValue: String
    fun clearPref()
}