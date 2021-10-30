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

package xyz.hisname.fireflyiii.ui.about

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.SystemInfoService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.userinfo.SystemInfoRepository

class AboutViewModel(application: Application): BaseViewModel(application) {

    private val systemInfoRepository by lazy { SystemInfoRepository(
            genericService().create(SystemInfoService::class.java),
            sharedPref(), newManager())
    }

    private val appPref by lazy { AppPref(sharedPref()) }

    var serverVersion = appPref.serverVersion
    var apiVersion = appPref.remoteApiVersion
    var userOs = appPref.userOs

    fun userSystem(): LiveData<Boolean> {
        val apiOk: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO+ CoroutineExceptionHandler { _, throwable ->
            apiOk.postValue(false)
        }){
            systemInfoRepository.getUserSystem()
            serverVersion = appPref.serverVersion
            apiVersion = appPref.remoteApiVersion
            userOs = appPref.userOs
            apiOk.postValue(true)
        }
        return apiOk
    }
}