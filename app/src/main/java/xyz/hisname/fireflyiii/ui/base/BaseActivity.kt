package xyz.hisname.fireflyiii.ui.base

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.viewmodel.GlobalViewModel
import xyz.hisname.fireflyiii.util.extension.getViewModel

@SuppressLint("Registered")
open class BaseActivity: AppCompatActivity() {

    val baseUrl: String by lazy { AppPref(this).getBaseUrl() }
    val accessToken: String by lazy { AppPref(this).getAccessToken() }
    private val globalViewModel by lazy { getViewModel(GlobalViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalViewModel.setUrl(baseUrl)
        globalViewModel.setToken(accessToken)
    }
}