package xyz.hisname.fireflyiii.ui.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.languagepack.LanguageChanger

@SuppressLint("Registered")
open class BaseActivity: AppCompatActivity() {

    private lateinit var prefListener: SharedPreferences.OnSharedPreferenceChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
    }

    private fun setTheme(){
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM){
            AppPref(sharedPref).nightModeEnabled = true
        }
        prefListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if(key == "night_mode"){
                if(sharedPref(this).nightModeEnabled){
                    delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    setTheme(R.style.AppTheme_Dark_DrawerTheme)
                } else {
                    setTheme(R.style.AppTheme)
                    delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
        if(sharedPref(this).nightModeEnabled){
            setTheme(R.style.AppTheme_Dark_DrawerTheme)
            delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        sharedPref.registerOnSharedPreferenceChangeListener(prefListener)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageChanger.init(newBase,
                sharedPref(newBase).languagePref))
    }

    protected fun sharedPref(context: Context): AppPref{
        return AppPref(PreferenceManager.getDefaultSharedPreferences(context))
    }
}