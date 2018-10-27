package xyz.hisname.fireflyiii.repository.models.currency

import androidx.room.PrimaryKey

data class CurrencyAttributes(
        @PrimaryKey(autoGenerate = true)
        val currencyIdPlaceholder: Long,
        val updated_at: String,
        val created_at: String,
        val name: String,
        val code: String,
        val symbol: String,
        val decimal_places: Int,
        val default: Boolean
)