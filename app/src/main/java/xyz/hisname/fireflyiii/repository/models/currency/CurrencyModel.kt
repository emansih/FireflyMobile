package xyz.hisname.fireflyiii.repository.models.currency

import androidx.room.Embedded
import androidx.room.Entity

@Entity
data class CurrencyModel(
        @Embedded
        val data: List<CurrencyData>,
        val meta: Meta
)