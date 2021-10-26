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
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.util.getUniqueHash

class GlobalViewModel(application: Application): AndroidViewModel(application) {

    var userEmail = ""
    var isDark: Boolean = false
        private set

    fun isDarkMode(): MutableLiveData<Boolean>{
        val darkModeLiveData: MutableLiveData<Boolean> = MutableLiveData()
        val appPreference = AppPref(getApplication<Application>().getSharedPreferences(
            getApplication<Application>().getUniqueHash().toString() + "-user-preferences", Context.MODE_PRIVATE))
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