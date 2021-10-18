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

package xyz.hisname.fireflyiii.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData

@Dao
abstract class CurrencyDataDao: BaseDao<CurrencyData> {

    @Query("SELECT * FROM currency ORDER BY name ASC LIMIT :currencyLimit")
    abstract fun getPaginatedCurrency(currencyLimit: Int): Flow<MutableList<CurrencyData>>

    @Query("SELECT * FROM currency ORDER BY currencyId")
    abstract fun getCurrency(): PagingSource<Int, CurrencyData>

    /* Sort currency according to their attributes
     * 1. Sort currency by their names in alphabetical order
     * 2. if currency has enabled = true as their attributes, put them at the top
     * 3. if currencyDefault = true, put the currency at the first row
     */
    @Query("SELECT * FROM (SELECT * FROM (SELECT * FROM currency ORDER BY name) ORDER BY enabled DESC) ORDER BY currencyDefault DESC")
    abstract suspend fun getSortedCurrency(): List<CurrencyData>

    @Query("DELETE FROM currency WHERE code = :currencyCode")
    abstract suspend fun deleteCurrencyByCode(currencyCode: String): Int

    @Query("SELECT * FROM currency WHERE code = :currencyCode")
    abstract suspend fun getCurrencyByCode(currencyCode: String): MutableList<CurrencyData>

    @Query("SELECT * FROM currency WHERE currencyDefault = :defaultCurrency")
    abstract fun getDefaultCurrency(defaultCurrency: Boolean = true): CurrencyData

    @Query("SELECT * FROM currency WHERE currencyId =:currencyId")
    abstract fun getCurrencyById(currencyId: Long): MutableList<CurrencyData>

    @Query("DELETE FROM currency WHERE currencyDefault =:defaultCurrency")
    abstract fun deleteDefaultCurrency(defaultCurrency: Boolean = true): Int

    @Query("DELETE FROM currency")
    abstract suspend fun deleteAllCurrency(): Int

    @Query("SELECT * FROM currency WHERE name =:currencyName")
    abstract fun getCurrencyByName(currencyName: String): MutableList<CurrencyData>

    @Update(entity = CurrencyData::class)
    abstract fun updateDefaultCurrency(currencyData: CurrencyData)

    @Query("UPDATE currency SET currencyDefault =:currencyDefault WHERE name =:currencyName")
    abstract fun changeDefaultCurrency(currencyName: String, currencyDefault: Boolean = true)

    @Query("SELECT currency.name FROM bills JOIN currency ON bills.currency_id = currency.currencyId WHERE billId =:billId")
    abstract suspend fun getCurrencyFromBill(billId: Long): String
}