package xyz.hisname.fireflyiii.repository.models.category

import androidx.room.Embedded
import androidx.room.Entity

@Entity
data class CategoryModel(
        @Embedded
        val data: MutableCollection<CategoryData>,
        val meta: Meta
)