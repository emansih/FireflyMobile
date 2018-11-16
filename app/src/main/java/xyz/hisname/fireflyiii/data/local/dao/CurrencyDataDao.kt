package xyz.hisname.fireflyiii.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData

@Dao
abstract class CurrencyDataDao: BaseDao<CurrencyData> {

    @Query("SELECT * FROM currency")
    abstract fun getAllCurrency(): LiveData<MutableList<CurrencyData>>

    @Query("SELECT * FROM currency")
    abstract fun getCurrency(): MutableList<CurrencyData>

    @Query("DELETE FROM currency WHERE currencyId = :currencyId")
    abstract fun deleteCurrencyById(currencyId: Long): Int

}