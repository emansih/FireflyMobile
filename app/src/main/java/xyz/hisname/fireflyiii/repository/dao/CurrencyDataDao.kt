package xyz.hisname.fireflyiii.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData

@Dao
abstract class CurrencyDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addCurrency(vararg currency: CurrencyData)

    @Query("SELECT * FROM currency")
    abstract fun getAllCurrency(): LiveData<MutableList<CurrencyData>>

}