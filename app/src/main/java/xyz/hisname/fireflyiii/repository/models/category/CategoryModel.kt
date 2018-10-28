package xyz.hisname.fireflyiii.repository.models.category

import androidx.room.Embedded
import androidx.room.Entity

@Entity
data class CategoryModel(
        @Embedded
        val data: List<CategoryData>,
        val links: Links,
        val meta: Meta
)