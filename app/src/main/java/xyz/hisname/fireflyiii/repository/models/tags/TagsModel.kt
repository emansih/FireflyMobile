package xyz.hisname.fireflyiii.repository.models.tags

import androidx.room.Embedded
import androidx.room.Entity
import com.squareup.moshi.JsonClass

@Entity
@JsonClass(generateAdapter = true)
data class TagsModel(
        @Embedded
        val data: List<TagsData>,
        val meta: Meta
)