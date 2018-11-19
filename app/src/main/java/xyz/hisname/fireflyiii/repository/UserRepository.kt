package xyz.hisname.fireflyiii.repository

import xyz.hisname.fireflyiii.data.local.pref.AppPref

class UserRepository(private val appPref: AppPref) {

    val baseUrl = appPref.getBaseUrl()
    val accessToken = appPref.getAccessToken()
    val clientId = appPref.getClientId()
    val clientSecret = appPref.getSecretKey()
    val refreshToken = appPref.getRefreshToken()

    fun insertAccessToken(accessToken: String){
        appPref.setAccessToken(accessToken)
    }

    fun insertRefreshToken(refreshToken: String){
        appPref.setRefreshToken(refreshToken)
    }

    fun insertTokenExpiry(expiry: Long){
        appPref.setTokenExpiry(expiry)
    }

}