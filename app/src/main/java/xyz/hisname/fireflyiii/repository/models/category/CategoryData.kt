package xyz.hisname.fireflyiii.repository.models.category

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "category")
data class CategoryData(
        @PrimaryKey(autoGenerate = false)
        @Json(name ="id")
        val categoryId: Long,
        @Embedded
        @Json(name ="attributes")
        val categoryAttributes: CategoryAttributes
)