package xyz.hisname.fireflyiii.ui.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.languagepack.LanguageChanger

@SuppressLint("Registered")
open class BaseActivity: AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageChanger.init(newBase,
                AppPref(PreferenceManager.getDefaultSharedPreferences(newBase)).languagePref))
    }

}