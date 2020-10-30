package xyz.hisname.fireflyiii.repository.models.category

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "category")
data class CategoryData(
        @PrimaryKey(autoGenerate = false)
        @Json(name ="id")
        var categoryId: Long? = null,
        @Embedded
        @Json(name ="attributes")
        var categoryAttributes: CategoryAttributes? = null,
        @Ignore
        val type: String = ""
)