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

package xyz.hisname.fireflyiii.repository

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref

class GlobalViewModel(application: Application): AndroidViewModel(application) {

    private lateinit var prefListener: SharedPreferences.OnSharedPreferenceChangeListener
    var isDark: Boolean = false
        private set

    fun isDarkMode(): MutableLiveData<Boolean>{
        val darkModeLiveData: MutableLiveData<Boolean> = MutableLiveData()
        val sharedPreferenceManager = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val appPreference = AppPref(PreferenceManager.getDefaultSharedPreferences(getApplication()))
        prefListener = SharedPreferences.OnSharedPreferenceChangeListener{ _, key ->
            if (key == "night_mode") {
                if (appPreference.nightModeEnabled) {
                    isDark =  true
                    darkModeLiveData.postValue(true)
                } else {
                    isDark = false
                    darkModeLiveData.postValue(false)
                }
            }
        }
        sharedPreferenceManager.registerOnSharedPreferenceChangeListener(prefListener)
        if(appPreference.nightModeEnabled){
            isDark =  true
            darkModeLiveData.postValue(true)
        } else {
            isDark =  false
            darkModeLiveData.postValue(false)
        }
        return darkModeLiveData
    }

}