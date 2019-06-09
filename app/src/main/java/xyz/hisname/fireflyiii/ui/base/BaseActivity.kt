package xyz.hisname.fireflyiii.ui.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.util.extension.getCompatColor
import xyz.hisname.languagepack.LanguageChanger
import java.util.*

@SuppressLint("Registered")
open class BaseActivity: AppCompatActivity() {

    private lateinit var prefListener: SharedPreferences.OnSharedPreferenceChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED){
            if(sharedPref(this).nightModeEnabled){
                enableDarkMode()
            } else {
                setTheme(R.style.AppTheme)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun setTheme(){
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        prefListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if(key == "night_mode"){
                if(sharedPref(this).nightModeEnabled){
                    enableDarkMode()
                } else {
                    setTheme(R.style.AppTheme)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
        if(sharedPref(this).nightModeEnabled){
            enableDarkMode()
        }
        sharedPref.registerOnSharedPreferenceChangeListener(prefListener)
    }

    private fun enableDarkMode(){
        setTheme(R.style.AppTheme_Dark_DrawerTheme)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = getCompatColor(R.color.md_black_1000)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageChanger.init(newBase,
                sharedPref(newBase).languagePref))
    }

    // 9 June 2019
    // FIXME: Bug in changing of language. Therefore we need these hacks here
    // Simplified and Traditional Chinese is broken if user uses _light mode_
    override fun applyOverrideConfiguration(overrideConfiguration: Configuration) {
        super.applyOverrideConfiguration(overrideConfiguration)
        onConfigurationChanged(overrideConfiguration)
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val res = this.resources
        val config = Configuration(res.configuration)
        config.locale = Locale(sharedPref(this).languagePref)
        res.updateConfiguration(config, res.displayMetrics)
    }

    protected fun sharedPref(context: Context): AppPref{
        return AppPref(PreferenceManager.getDefaultSharedPreferences(context))
    }
}