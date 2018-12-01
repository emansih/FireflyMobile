package xyz.hisname.fireflyiii.workers

import android.accounts.AccountManager
import android.content.Context
import android.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder

abstract class BaseWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams){

    private val baseUrl by lazy { AppPref(sharedPref).baseUrl }
    private val accessToken by lazy { AuthenticatorManager(AccountManager.get(context)).accessToken }
    val genericService by lazy { RetrofitBuilder.getClient(baseUrl,accessToken, getPinValue()) }
    protected val sharedPref by lazy {  PreferenceManager.getDefaultSharedPreferences(context) }

    private fun getPinValue(): String {
        var cert = ""
        if(AppPref(sharedPref).enableCertPinning) {
            cert = AppPref(sharedPref).certValue
        }
        return cert
    }
}