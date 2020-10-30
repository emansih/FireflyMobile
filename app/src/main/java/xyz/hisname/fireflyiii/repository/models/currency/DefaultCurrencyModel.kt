package xyz.hisname.fireflyiii.repository.models.currency

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class DefaultCurrencyModel(
        val data: CurrencyData
)