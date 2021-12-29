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
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import retrofit2.Retrofit
import xyz.hisname.fireflyiii.data.local.account.NewAccountManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.util.network.CustomCa
import java.io.File
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

abstract class BaseWorker(private val context: Context, workerParams: WorkerParameters): CoroutineWorker(context, workerParams){

    val uuid = inputData.getString("uuid") ?: ""

    private fun getTrust(customCa: CustomCa, customCaFile: File): X509TrustManager?{
        return if(customCaFile.exists()){
            customCa.getCustomTrust()
        } else {
            null
        }
    }

    private fun getSslSocket(customCa: CustomCa, customCaFile: File): SSLSocketFactory?{
        return if(customCaFile.exists()){
            customCa.getCustomSSL()
        } else {
            null
        }
    }

    protected fun genericService(uuid: String): Retrofit{
        val baseUrl = appPref(uuid).baseUrl
        val customCaFile = File(context.filesDir.path + "/" + uuid + ".pem")
        val customCa = CustomCa(customCaFile)
        val accessToken = NewAccountManager(AccountManager.get(context), uuid).accessToken
        return FireflyClient.getClient(baseUrl, accessToken, appPref(uuid).certValue,
            getTrust(customCa, customCaFile), getSslSocket(customCa, customCaFile))
    }

    protected fun appPref(uuid: String): AppPref {
        val sharedPref = context.getSharedPreferences("$uuid-user-preferences", Context.MODE_PRIVATE)
        return AppPref(sharedPref)
    }
}