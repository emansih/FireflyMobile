package xyz.hisname.fireflyiii.repository.models.currency

import androidx.room.Embedded
import androidx.room.Entity

@Entity
data class CurrencyModel(
        @Embedded
        val data: MutableCollection<CurrencyData>,
        val meta: Meta
)