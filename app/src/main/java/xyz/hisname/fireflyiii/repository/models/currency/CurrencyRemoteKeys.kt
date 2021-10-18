package xyz.hisname.fireflyiii.repository.models.currency

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currency_remote_keys")
data class CurrencyRemoteKeys(
    @PrimaryKey
    val pagingPrimaryKey: Int,
    val nextPageKey: Int
)
