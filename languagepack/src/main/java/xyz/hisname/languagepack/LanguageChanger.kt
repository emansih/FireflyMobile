package xyz.hisname.languagepack

import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import androidx.core.os.ConfigurationCompat
import java.util.*

class LanguageChanger(baseContext: Context): ContextWrapper(baseContext){

    companion object {

        private lateinit var locale: Locale
        private lateinit var config: Configuration
// Code adapted from: https://stackoverflow.com/questions/40221711/android-context-getresources-updateconfiguration-deprecated
        fun init(context: Context, language: String?): ContextWrapper{
            config = context.resources.configuration
            locale = if(language.isNullOrEmpty()) {
                Locale(ConfigurationCompat.getLocales(config)[0].language)
            } else {
                Locale(language)
            }
            Locale.setDefault(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
               setSystemLocale()
            } else {
                setSystemLocaleLegacy()
            }
            return LanguageChanger(context.createConfigurationContext(config))
        }

        @Suppress("DEPRECATION")
        private fun setSystemLocaleLegacy() {
            config.locale = locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        private fun setSystemLocale() {
            config.setLocale(locale)
        }
    }
}