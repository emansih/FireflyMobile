package xyz.hisname.fireflyiii.repository.models.tags

import androidx.room.Embedded
import androidx.room.Entity

@Entity
data class TagsModel(
        @Embedded
        val data: List<TagsData>,
        val meta: Meta
)