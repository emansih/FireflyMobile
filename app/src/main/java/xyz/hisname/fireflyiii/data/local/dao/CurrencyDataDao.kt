package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData

@Dao
abstract class CurrencyDataDao: BaseDao<CurrencyData> {

    @Query("SELECT * FROM currency ORDER BY name ASC LIMIT :currencyLimit")
    abstract fun getPaginatedCurrency(currencyLimit: Int): Flow<MutableList<CurrencyData>>

    @Query("SELECT * FROM currency")
    abstract suspend fun getCurrency(): List<CurrencyData>

    @Query("SELECT COUNT(*) FROM currency")
    abstract suspend fun getCurrencyCount(): Int

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
    abstract fun getDefaultCurrency(defaultCurrency: Boolean = true): MutableList<CurrencyData>

    @Query("SELECT * FROM currency WHERE currencyId =:currencyId")
    abstract fun getCurrencyById(currencyId: Long): MutableList<CurrencyData>

    @Query("DELETE FROM currency WHERE currencyDefault =:defaultCurrency")
    abstract fun deleteDefaultCurrency(defaultCurrency: Boolean = true): Int

    @Query("DELETE FROM currency")
    abstract fun deleteAllCurrency(): Int

    @Query("SELECT * FROM currency WHERE name =:currencyName")
    abstract fun getCurrencyByName(currencyName: String): MutableList<CurrencyData>

    @Update(entity = CurrencyData::class)
    abstract fun updateDefaultCurrency(currencyData: CurrencyData)

    @Query("UPDATE currency SET currencyDefault =:currencyDefault WHERE name =:currencyName")
    abstract fun changeDefaultCurrency(currencyName: String, currencyDefault: Boolean = true)
}