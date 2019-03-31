package xyz.hisname.fireflyiii.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData

@Dao
abstract class CurrencyDataDao: BaseDao<CurrencyData> {

    @Query("SELECT * FROM currency")
    abstract fun getAllCurrency(): MutableList<CurrencyData>

    @Query("DELETE FROM currency WHERE currencyId = :currencyId")
    abstract fun deleteCurrencyById(currencyId: Long): Int

    @Query("SELECT * FROM currency WHERE code = :currencyCode")
    abstract fun getCurrencyByCode(currencyCode: String): MutableList<CurrencyData>

    @Query("SELECT * FROM currency WHERE currencyDefault = :defaultCurrency")
    abstract fun getDefaultCurrency(defaultCurrency: Boolean = true): MutableList<CurrencyData>

    @Query("SELECT * FROM currency WHERE enabled = :enabled")
    abstract fun getEnabledCurrencyByCode(enabled: Boolean = true): LiveData<MutableList<CurrencyData>>

    @Query("SELECT * FROM currency WHERE currencyId =:currencyId")
    abstract fun getCurrencyById(currencyId: Long): MutableList<CurrencyData>

    @Query("DELETE FROM currency WHERE currencyDefault =:defaultCurrency")
    abstract fun deleteDefaultCurrency(defaultCurrency: Boolean = true): Int

    @Query("DELETE FROM currency")
    abstract fun deleteAllCurrency(): Int
}