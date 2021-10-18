package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyRemoteKeys

@Dao
abstract class CurrencyKeyDao: BaseDao<CurrencyRemoteKeys> {

    @Query("SELECT * FROM currency_remote_keys")
    abstract suspend fun remoteKey(): CurrencyRemoteKeys

    @Query("DELETE FROM currency_remote_keys")
    abstract suspend fun deleteCurrencyKey()

}