package xyz.hisname.fireflyiii.repository.models.category

import androidx.room.Embedded
import androidx.room.Entity
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity
data class CategoryModel(
        @Embedded
        val data: MutableList<CategoryData>,
        val meta: Meta
)