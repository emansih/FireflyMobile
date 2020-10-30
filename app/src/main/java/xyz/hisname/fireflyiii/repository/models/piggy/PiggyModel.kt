package xyz.hisname.fireflyiii.repository.models.piggy

import androidx.room.Embedded
import androidx.room.Entity
import com.squareup.moshi.JsonClass

@Entity
@JsonClass(generateAdapter = true)
data class PiggyModel(
        @Embedded
        val data: MutableCollection<PiggyData>,
        val meta: Meta
)