package xyz.hisname.fireflyiii.data.local.pref

interface PreferenceHelper {

    var baseUrl: String
    var isTransactionPersistent: Boolean
    var userRole: String
    var remoteApiVersion: String
    var serverVersion: String
    var userOs: String
    var certValue: String
    var languagePref: String
    var nightModeEnabled: Boolean
    var isKeyguardEnabled: Boolean
    var transactionListType: Boolean
    var timeFormat: Boolean
    var isCustomCa: Boolean
    var isCurrencyThumbnailEnabled: Boolean
    var workManagerDelay: Long
    var workManagerLowBattery: Boolean
    fun clearPref()
}