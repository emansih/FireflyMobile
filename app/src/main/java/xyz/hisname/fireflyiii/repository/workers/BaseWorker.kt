package xyz.hisname.fireflyiii.repository.workers

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import xyz.hisname.fireflyiii.repository.RetrofitBuilder

abstract class BaseWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams){

    private val sharedPref: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
    private val baseUrl: String by lazy { sharedPref.getString("fireflyUrl", "") }
    private val accessToken: String by lazy { sharedPref.getString("access_token","") }
    val genericService by lazy { RetrofitBuilder.getClient(baseUrl,accessToken) }
}