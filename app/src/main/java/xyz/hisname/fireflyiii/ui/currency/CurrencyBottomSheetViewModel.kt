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

package xyz.hisname.fireflyiii.ui.currency

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyRemoteMediator

class CurrencyBottomSheetViewModel(application: Application): BaseViewModel(application) {

    val currencyCode = MutableLiveData<String>()
    val currencyFullDetails = MutableLiveData<String>()

    private val databaseInstance = AppDatabase.getInstance(application)
    private val currencyDao = databaseInstance.currencyDataDao()
    private val currencyService = genericService().create(CurrencyService::class.java)
    private val currencyRemoteKeyDao = databaseInstance.currencyRemoteKeysDao()

    @OptIn(ExperimentalPagingApi::class)
    fun getCurrencyList() = Pager(
        config = PagingConfig(pageSize = Constants.PAGE_SIZE),
        remoteMediator = CurrencyRemoteMediator(currencyDao, currencyService, currencyRemoteKeyDao)
    ){
        currencyDao.getCurrency()
    }.flow.cachedIn(viewModelScope).asLiveData()
}