package xyz.hisname.fireflyiii.repository.models.currency

import com.google.gson.annotations.SerializedName

data class CurrencySuccessModel(
        @SerializedName("data")
        val data: CurrencyData
)