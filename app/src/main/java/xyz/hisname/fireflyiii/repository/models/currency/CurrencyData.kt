package xyz.hisname.fireflyiii.repository.models.currency

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "currency")
data class CurrencyData(
        @PrimaryKey(autoGenerate = false)
        @SerializedName("id")
        var id: String = "",
        @Embedded
        @SerializedName("attributes")
        var currencyAttributes: CurrencyAttributes? = null,
        @Ignore
        val type: String = ""
)