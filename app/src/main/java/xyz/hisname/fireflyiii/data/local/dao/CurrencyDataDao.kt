package xyz.hisname.fireflyiii.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData

@Dao
abstract class CurrencyDataDao: BaseDao<CurrencyData> {

    @Query("SELECT * FROM currency ORDER BY name ASC LIMIT :currencyLimit")
    abstract fun getPaginatedCurrency(currencyLimit: Int): Flow<MutableList<CurrencyData>>

    @Query("DELETE FROM currency WHERE code = :currencyCode")
    abstract fun deleteCurrencyByCode(currencyCode: String): Int

    @Query("SELECT * FROM currency WHERE code = :currencyCode")
    abstract fun getCurrencyByCode(currencyCode: String): MutableList<CurrencyData>

    @Query("SELECT * FROM currency WHERE currencyDefault = :defaultCurrency")
    abstract fun getDefaultCurrency(defaultCurrency: Boolean = true): MutableList<CurrencyData>

    @Query("SELECT * FROM currency WHERE enabled = :enabled ORDER BY name ASC")
    abstract fun getEnabledCurrencyByCode(enabled: Boolean = true): LiveData<MutableList<CurrencyData>>

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