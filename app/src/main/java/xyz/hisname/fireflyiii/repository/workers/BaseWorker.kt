package xyz.hisname.fireflyiii.repository.workers

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters

abstract class BaseWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams){

    private val sharedPref: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
    val baseUrl: String by lazy { sharedPref.getString("fireflyUrl", "") }
    val accessToken: String by lazy { sharedPref.getString("access_token","") }

}