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

package xyz.hisname.fireflyiii.repository

import android.accounts.AccountManager
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import retrofit2.Retrofit
import xyz.hisname.fireflyiii.data.local.account.NewAccountManager
import xyz.hisname.fireflyiii.data.local.dao.FireflyUserDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.util.network.CustomCa
import java.io.File

open class BaseViewModel(application: Application) : AndroidViewModel(application){

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val apiResponse: MutableLiveData<String> = MutableLiveData()

    protected fun newManager(): NewAccountManager{
        return NewAccountManager(AccountManager.get(getApplication()), getUniqueHash())
    }


    protected fun sharedPref() =
        getApplication<Application>().getSharedPreferences(
            getUniqueHash() + "-user-preferences", Context.MODE_PRIVATE)

    protected fun genericService(): Retrofit {
        val cert = AppPref(sharedPref()).certValue
        val certFile = File(getApplication<Application>().filesDir.path + "/" + getUniqueHash() + ".pem")
        return if (certFile.exists()) {
            val customCa = CustomCa(certFile)
            FireflyClient.getClient(getActiveUserUrl(), newManager().accessToken, cert,
                customCa.getCustomTrust(), customCa.getCustomSSL())
        } else {
            FireflyClient.getClient(getActiveUserUrl(), newManager().accessToken, cert, null, null)
        }
    }

    protected fun getUniqueHash(): String {
        val uniqueHash: String
        runBlocking(Dispatchers.IO){
            uniqueHash = FireflyUserDatabase.getInstance(getApplication()).fireflyUserDao().getUniqueHash()
        }
        return uniqueHash
    }

    protected fun getActiveUserEmail(): String {
        val activeUserEmail: String
        runBlocking(Dispatchers.IO){
            activeUserEmail = FireflyUserDatabase.getInstance(getApplication()).fireflyUserDao().getCurrentActiveUserEmail()
        }
        return activeUserEmail
    }

    private fun getActiveUserUrl(): String {
        val activeUrl: String
        runBlocking(Dispatchers.IO){
            activeUrl = FireflyUserDatabase.getInstance(getApplication()).fireflyUserDao().getCurrentActiveUserUrl()
        }
        return activeUrl
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}