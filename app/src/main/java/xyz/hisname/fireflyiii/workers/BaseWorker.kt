package xyz.hisname.fireflyiii.workers

import android.accounts.AccountManager
import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient

abstract class BaseWorker(context: Context, workerParams: WorkerParameters): CoroutineWorker(context, workerParams){

    private val baseUrl by lazy { AppPref(sharedPref).baseUrl }
    private val accessToken by lazy { AuthenticatorManager(AccountManager.get(context)).accessToken }
    val genericService by lazy { FireflyClient.getClient(baseUrl,accessToken, getPinValue()) }
    protected val sharedPref by lazy {  PreferenceManager.getDefaultSharedPreferences(context) }

    private fun getPinValue(): String {
        var cert = ""
        if(AppPref(sharedPref).enableCertPinning) {
            cert = AppPref(sharedPref).certValue
        }
        return cert
    }
}