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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.remote.firefly.api.DataService

class DestroyItemsViewModel(application: Application): BaseViewModel(application)   {

    val message: MutableLiveData<String> = MutableLiveData()

    fun deleteObject(objectToDelete: String){
        viewModelScope.launch(Dispatchers.IO) {
            val networkCall = genericService().create(DataService::class.java).destroyItem(objectToDelete)
            if(networkCall.code() == 204){
                message.postValue("Deleted")
            } else {
                message.postValue("There was an issue deleting")
            }
        }
    }
}