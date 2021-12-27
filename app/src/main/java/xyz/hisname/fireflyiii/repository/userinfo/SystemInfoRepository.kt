/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.repository.userinfo

import android.accounts.AccountManager
import android.content.SharedPreferences
import xyz.hisname.fireflyiii.data.local.account.NewAccountManager
import xyz.hisname.fireflyiii.data.local.dao.FireflyUserDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.SystemInfoService

class SystemInfoRepository(private val systemInfoService: SystemInfoService?,
                           private val sharedPreferences: SharedPreferences,
                           private val authenticationManager: NewAccountManager) {

    // Currently this method does too much things. "Should" refactor
    @Throws(Exception::class)
    suspend fun getCurrentUserInfo(baseUrl: String, accountManager: AccountManager,
                                   fireflyUserDatabase: FireflyUserDatabase){
        val userAttribute = systemInfoService?.getCurrentUserInfo()?.body()?.userData?.userAttributes
        if (userAttribute != null) {
            val authMethod = authenticationManager.authMethod
            val newAccountManager = NewAccountManager(accountManager, fireflyUserDatabase.fireflyUserDao().getUniqueHash())
            newAccountManager.authMethod = authMethod
            fireflyUserDatabase.fireflyUserDao().updateActiveUserEmail(userAttribute.email)
            fireflyUserDatabase.fireflyUserDao().updateActiveUserHost(baseUrl)
            // On single account systems, role will null
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