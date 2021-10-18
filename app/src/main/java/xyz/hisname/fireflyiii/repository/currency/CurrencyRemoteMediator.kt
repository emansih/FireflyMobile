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

package xyz.hisname.fireflyiii.repository.currency

import androidx.paging.*
import retrofit2.HttpException
import xyz.hisname.fireflyiii.data.local.dao.CurrencyDataDao
import xyz.hisname.fireflyiii.data.local.dao.CurrencyKeyDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyRemoteKeys
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class CurrencyRemoteMediator(private val currencyDataDao: CurrencyDataDao,
                             private val currencyService: CurrencyService,
                             private val currencyRemoteKeysDataDao: CurrencyKeyDao): RemoteMediator<Int, CurrencyData>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, CurrencyData>): MediatorResult {
        try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> {
                    currencyRemoteKeysDataDao.deleteCurrencyKey()
                    null
                }
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                   currencyRemoteKeysDataDao.remoteKey().nextPageKey
                }
            }

            val networkCall = currencyService.getPaginatedCurrency(loadKey ?: 1)

            if (loadType == LoadType.REFRESH) {
                currencyDataDao.deleteAllCurrency()
                currencyRemoteKeysDataDao.deleteCurrencyKey()
            }

            val isSuccessful = networkCall.isSuccessful
            val currencyBody = networkCall.body()
            if(isSuccessful && currencyBody != null){
                if(loadKey == 1){
                    currencyRemoteKeysDataDao.deleteCurrencyKey()
                }
                currencyRemoteKeysDataDao.insert(CurrencyRemoteKeys(currencyBody.meta.pagination.current_page,
                    currencyBody.meta.pagination.current_page + 1))

                currencyBody.data.forEach { currencyData ->
                    currencyDataDao.insert(currencyData)
                }
            }

            return MediatorResult.Success(endOfPaginationReached = currencyBody?.data?.isEmpty() ?: true)
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

}