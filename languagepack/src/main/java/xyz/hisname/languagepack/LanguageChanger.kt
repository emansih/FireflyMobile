/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.languagepack

import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import java.util.*

class LanguageChanger(baseContext: Context): ContextWrapper(baseContext){

    companion object {

        private lateinit var locale: Locale
        private lateinit var config: Configuration
        private val supportedLocale by lazy { arrayListOf("en", "de", "es", "fr", "it", "nl", "ru", "zh-rCN", "zh-rTW", "pt-rBR") }

        // Code adapted from: https://stackoverflow.com/questions/40221711/android-context-getresources-updateconfiguration-deprecated

        fun init(context: Context, language: String?): ContextWrapper{
            config = context.resources.configuration
            if(supportedLocale.contains(language)) {
                locale = when {
                    language.isNullOrEmpty() -> Locale("en")
                    language == "zh-rCN" -> Locale("zh", "CN")
                    language == "zh-rTW" -> Locale("zh", "TW")
                    language == "pt-rBR" -> Locale("pt", "BR")
                    else -> Locale(language)
                }
            } else {
                locale = Locale("en")
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

        fun getLocale(context: Context): Locale{
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales.get(0)
            } else {
                context.resources.configuration.locale
            }
        }
    }
}