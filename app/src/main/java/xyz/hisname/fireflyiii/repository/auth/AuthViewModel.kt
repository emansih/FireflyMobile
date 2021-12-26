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

package xyz.hisname.fireflyiii.repository.auth

import android.accounts.AccountManager
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.account.NewAccountManager
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.OAuthService
import xyz.hisname.fireflyiii.repository.BaseViewModel

class AuthViewModel(application: Application): BaseViewModel(application) {

    private val isAuthenticated: MutableLiveData<Boolean> = MutableLiveData()

    private val oAuthService by lazy { genericService().create(OAuthService::class.java) }

    fun getRefreshToken(): LiveData<Boolean> {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val newAccountManager = NewAccountManager(AccountManager.get(getApplication()), getUniqueHash())
                val networkCall = oAuthService.getRefreshToken("refresh_token",
                    newAccountManager.refreshToken, newAccountManager.clientId,
                    newAccountManager.secretKey)
                val authResponse = networkCall.body()
                if (authResponse != null && networkCall.isSuccessful) {
                    newAccountManager.accessToken = authResponse.access_token
                    newAccountManager.refreshToken = authResponse.refresh_token
                    newAccountManager.tokenExpiry = authResponse.expires_in
                    isAuthenticated.postValue(true)
                    FireflyClient.destroyInstance()
                } else {
                    isAuthenticated.postValue(false)
                }
            }
        } catch (exception: Exception) {
            isAuthenticated.postValue(false)
        }
        return isAuthenticated
    }
}