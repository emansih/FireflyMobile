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

package xyz.hisname.fireflyiii.ui.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.GlobalViewModel
import xyz.hisname.fireflyiii.util.extension.getCompatColor
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.languagepack.LanguageChanger

@SuppressLint("Registered")
open class BaseActivity: AppCompatActivity() {

    protected val globalViewModel by lazy { getViewModel(GlobalViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
    }

    private fun setTheme(){
        globalViewModel.isDarkMode().observe(this){ isDark ->
            if(isDark){
                setTheme(R.style.AppTheme_Dark_DrawerTheme)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                window.navigationBarColor = getCompatColor(R.color.md_black_1000)
            } else {
                setTheme(R.style.AppTheme)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageChanger.init(newBase,
                sharedPref(newBase).languagePref))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration) {
        val uiMode = overrideConfiguration.uiMode
        overrideConfiguration.setTo(baseContext.resources.configuration)
        overrideConfiguration.uiMode = uiMode
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    protected fun sharedPref(context: Context): AppPref{
        return AppPref(PreferenceManager.getDefaultSharedPreferences(context))
    }
}