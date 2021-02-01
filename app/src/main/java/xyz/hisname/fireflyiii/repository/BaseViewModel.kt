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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import retrofit2.Retrofit
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.util.network.CustomCa
import java.io.File

open class BaseViewModel(application: Application) : AndroidViewModel(application){

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val apiResponse: MutableLiveData<String> = MutableLiveData()
    protected val accManager by lazy { AuthenticatorManager(AccountManager.get(getApplication()))  }
    protected val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(getApplication()) }

    protected fun genericService(): Retrofit {
        val cert = AppPref(sharedPref).certValue
        return if (AppPref(sharedPref).isCustomCa) {
            val customCa = CustomCa(File(getApplication<Application>().filesDir.path + "/user_custom.pem"))
            FireflyClient.getClient(AppPref(sharedPref).baseUrl,
                    accManager.accessToken, cert, customCa.getCustomTrust(), customCa.getCustomSSL())
        } else {
            FireflyClient.getClient(AppPref(sharedPref).baseUrl,
                    accManager.accessToken, cert, null, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}