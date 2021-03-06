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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyPagingSource
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.DeleteCurrencyWorker

class CurrencyListViewModel(application: Application): BaseViewModel(application) {

    private val currencyDao = AppDatabase.getInstance(application).currencyDataDao()
    private val currencyService = genericService().create(CurrencyService::class.java)
    private val currencyRepository = CurrencyRepository(currencyDao, currencyService)

    fun getCurrencyList() =
        Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)){
            CurrencyPagingSource(currencyDao, currencyService)
        }.flow.cachedIn(viewModelScope).asLiveData()


    fun deleteCurrency(currencyCode: String): LiveData<Boolean> {
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        var isItDeleted = 0
        viewModelScope.launch(Dispatchers.IO){
            val currencyList = currencyRepository.getCurrencyByCode(currencyCode)
            if(currencyList.isNotEmpty()){
                isItDeleted = currencyRepository.deleteCurrencyByCode(currencyCode)
            }
            // Since onDraw() is being called multiple times, we check if the currency exists locally in the DB.
            when (isItDeleted) {
                HttpConstants.FAILED -> {
                    isDeleted.postValue(false)
                    val currencyId = currencyList[0].currencyId
                    if(currencyId != 0L){
                        DeleteCurrencyWorker.initPeriodicWorker(currencyId, getApplication())
                    }
                }
                HttpConstants.UNAUTHORISED -> {
                    isDeleted.postValue(false)
                }
                HttpConstants.NO_CONTENT_SUCCESS -> {
                    isDeleted.postValue(true)
                }
            }
        }
        return isDeleted
    }
}