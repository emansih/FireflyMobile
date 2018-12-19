package xyz.hisname.fireflyiii.repository.models.currency

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class CurrencyAttributes(
        @PrimaryKey(autoGenerate = true)
        val currencyIdPlaceHolder: Long,
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