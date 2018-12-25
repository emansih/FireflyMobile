package xyz.hisname.fireflyiii.repository.models.currency

import androidx.room.Entity
import com.google.gson.annotations.SerializedName

@Entity
data class CurrencyAttributes(
        val updated_at: String,
        val created_at: String,
        val enabled: Boolean,
        val name: String,
        val code: String,
        val symbol: String,
        val decimal_places: Int,
        @SerializedName("default")
        val currencyDefault: Boolean
)