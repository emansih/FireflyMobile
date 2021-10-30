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
import java.util.*

open class BaseViewModel(application: Application) : AndroidViewModel(application){

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val apiResponse: MutableLiveData<String> = MutableLiveData()

    protected fun newManager() =
        NewAccountManager(AccountManager.get(getApplication()),
            FireflyUserDatabase.getInstance(getApplication()).fireflyUserDao().getCurrentActiveUserEmail())

    protected fun sharedPref() =
        getApplication<Application>().getSharedPreferences(
            getUniqueHash().toString() + "-user-preferences", Context.MODE_PRIVATE)


    protected fun genericService(): Retrofit {
        val cert = AppPref(sharedPref()).certValue
        val fireflyUrl = FireflyUserDatabase.getInstance(getApplication()).fireflyUserDao().getCurrentActiveUserUrl()
        return if (AppPref(sharedPref()).isCustomCa) {
            val customCa = CustomCa(File(getApplication<Application>().filesDir.path + "/" + getUniqueHash() + ".pem"))
            FireflyClient.getClient(fireflyUrl, newManager().accessToken, cert,
                customCa.getCustomTrust(), customCa.getCustomSSL())
        } else {
            FireflyClient.getClient(fireflyUrl, newManager().accessToken, cert, null, null)
        }
    }

    protected fun getUniqueHash(): UUID {
        return UUID.fromString(
            FireflyUserDatabase.getInstance(getApplication()).fireflyUserDao().getUniqueHash()
        )
    }

    protected fun getActiveUserEmail(): String {
        return FireflyUserDatabase.getInstance(getApplication()).fireflyUserDao().getCurrentActiveUserEmail()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}