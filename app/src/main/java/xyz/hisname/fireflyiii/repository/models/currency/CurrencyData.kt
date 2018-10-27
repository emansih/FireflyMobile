package xyz.hisname.fireflyiii.repository.models.currency

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "currency")
data class CurrencyData(
        @Ignore
        var type: String = "",
        @PrimaryKey(autoGenerate = false)
        @SerializedName("id")
        var currencyId: Long? = null,
        @SerializedName("attributes")
        var currencyAttributes: CurrencyAttributes? = null)