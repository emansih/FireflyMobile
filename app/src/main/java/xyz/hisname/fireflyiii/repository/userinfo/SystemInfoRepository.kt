package xyz.hisname.fireflyiii.repository.userinfo

import android.content.SharedPreferences
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.SystemInfoService

class SystemInfoRepository(private val systemInfoService: SystemInfoService?,
                           private val sharedPreferences: SharedPreferences,
                           private val authenticationManager: AuthenticatorManager) {

    @Throws(Exception::class)
    suspend fun getCurrentUserInfo(){
        val userAttribute = systemInfoService?.getCurrentUserInfo()?.body()?.userData?.userAttributes
        if (userAttribute != null) {
            authenticationManager.userEmail = userAttribute.email
            if(userAttribute.role != null){
                AppPref(sharedPreferences).userRole = userAttribute.role
            }
        } else {
            throw Exception("Failed to fetch data")
        }
    }

    @Throws(Exception::class)
    suspend fun getUserSystem(){
        val systemInfoModel = systemInfoService?.getSystemInfo()?.body()
        val systemData = systemInfoModel?.systemData
        if (systemData != null) {
            AppPref(sharedPreferences).serverVersion = systemData.version
            AppPref(sharedPreferences).remoteApiVersion = systemData.api_version
            AppPref(sharedPreferences).userOs = systemData.os
        } else {
            throw Exception("Failed to fetch data")
        }
    }
}