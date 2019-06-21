package xyz.hisname.fireflyiii.repository.models.category

import androidx.room.Entity
import androidx.room.Fts4


@Fts4(contentEntity = CategoryData::class)
@Entity(tableName = "categoryFts")
data class CategoryFts(
        val name: String,
        val categoryId: String
)