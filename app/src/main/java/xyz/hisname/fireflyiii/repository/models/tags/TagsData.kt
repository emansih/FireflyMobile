package xyz.hisname.fireflyiii.repository.models.tags

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Entity(tableName = "tags")
@JsonClass(generateAdapter = true)
data class TagsData(
        @Embedded
        @Json(name ="attributes")
        var tagsAttributes: TagsAttributes? = null,
        @Json(name ="id")
        @PrimaryKey(autoGenerate = false)
        var tagsId: Long? = null,
        @Ignore
        var type: String =""
)