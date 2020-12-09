package xyz.hisname.fireflyiii.repository.models.currency

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "currency")
data class CurrencyData(
        @PrimaryKey(autoGenerate = false)
        @Json(name ="id")
        var currencyId: Long = 0,
        @Embedded
        @Json(name ="attributes")
        var currencyAttributes: CurrencyAttributes? = null
)