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

package xyz.hisname.fireflyiii.workers

import android.accounts.AccountManager
import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.util.network.CustomCa
import java.io.File
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

abstract class BaseWorker(context: Context, workerParams: WorkerParameters): CoroutineWorker(context, workerParams){

    private val baseUrl by lazy { AppPref(sharedPref).baseUrl }
    private val accessToken by lazy { AuthenticatorManager(AccountManager.get(context)).accessToken }
    val genericService by lazy { FireflyClient.getClient(baseUrl,accessToken, AppPref(sharedPref).certValue, getTrust(), getSslSocket()) }
    protected val sharedPref by lazy {  PreferenceManager.getDefaultSharedPreferences(context) }
    private val customCa by lazy { CustomCa(File(context.filesDir.path + "/user_custom.pem")) }

    private fun getTrust(): X509TrustManager?{
        return if(AppPref(sharedPref).isCustomCa){
            customCa.getCustomTrust()
        } else {
            null
        }
    }

    private fun getSslSocket(): SSLSocketFactory?{
        return if(AppPref(sharedPref).isCustomCa){
            customCa.getCustomSSL()
        } else {
            null
        }
    }
}