package xyz.hisname.fireflyiii.util

import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import java.util.*
import androidx.core.os.ConfigurationCompat


class LangContextWrapper(base: Context): ContextWrapper(base) {

    companion object {
        fun wrap(context: Context): ContextWrapper{
            val config = context.resources.configuration
            val locale = Locale(ConfigurationCompat.getLocales(context.resources.configuration)[0].language)
            Locale.setDefault(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setSystemLocale(config, locale)
            } else {
                setSystemLocaleLegacy(config, locale)
            }
            return LangContextWrapper(context.createConfigurationContext(config))
        }

        @Suppress("DEPRECATION")
        private fun setSystemLocaleLegacy(config: Configuration, locale: Locale) {
            config.locale = locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        private fun setSystemLocale(config: Configuration, locale: Locale) {
            config.setLocale(locale)
        }

    }

}