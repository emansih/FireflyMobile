package xyz.hisname.fireflyiii.repository.models.autocomplete

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PiggybankItems(
        val id: Long,
        val name: String,
        val name_with_balance: String,
        val currency_id: Long,
        val currency_code: String,
        val currency_symbol: String,
        val currency_decimal_places: Int
)