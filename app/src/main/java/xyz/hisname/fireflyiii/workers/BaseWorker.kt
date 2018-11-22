package xyz.hisname.fireflyiii.workers

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder

abstract class BaseWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams){

    private val baseUrl by lazy { AppPref(context).baseUrl }
    private val accessToken by lazy { AppPref(context).accessToken }
    val genericService by lazy { RetrofitBuilder.getClient(baseUrl,accessToken, getPinValue()) }

    private fun getPinValue(): String {
        var cert = ""
        if(AppPref(applicationContext).enableCertPinning) {
            cert = AppPref(applicationContext).certValue
        }
        return cert
    }
}