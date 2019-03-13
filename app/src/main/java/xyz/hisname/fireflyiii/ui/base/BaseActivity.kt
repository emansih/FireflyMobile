package xyz.hisname.fireflyiii.ui.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.GlobalViewModel
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.languagepack.LanguageChanger

@SuppressLint("Registered")
open class BaseActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getViewModel(GlobalViewModel::class.java).nightMode.observe(this, Observer { enabled ->
            if(enabled){
                delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        })
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageChanger.init(newBase,
                AppPref(PreferenceManager.getDefaultSharedPreferences(newBase)).languagePref))
    }

}