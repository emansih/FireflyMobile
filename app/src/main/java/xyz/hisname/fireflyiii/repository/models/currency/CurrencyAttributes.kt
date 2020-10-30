package xyz.hisname.fireflyiii.repository.models.currency

import androidx.room.Entity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Entity
@JsonClass(generateAdapter = true)
data class CurrencyAttributes(
        val updated_at: String,
        val created_at: String,
        val enabled: Boolean,
        val name: String,
        val code: String,
        val symbol: String,
        val decimal_places: Int,
        @Json(name ="default")
        val currencyDefault: Boolean
)