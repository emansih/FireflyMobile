package xyz.hisname.fireflyiii.repository.models.piggy

import androidx.room.Embedded
import androidx.room.Entity

@Entity
data class PiggyModel(
        @Embedded
        val data: MutableCollection<PiggyData>,
        val meta: Meta
)