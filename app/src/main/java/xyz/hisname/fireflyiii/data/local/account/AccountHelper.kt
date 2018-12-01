package xyz.hisname.fireflyiii.data.local.account

interface AccountHelper {

    var secretKey: String
    var accessToken: String
    var clientId: String
    var refreshToken: String
    var tokenExpiry: Long
    var authMethod: String
    var userEmail: String
    fun initializeAccount()
    fun destroyAccount()
    fun isTokenValid(): Boolean
}