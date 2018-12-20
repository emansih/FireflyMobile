package xyz.hisname.fireflyiii.repository.models.tags

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TagsAttributes(
        @PrimaryKey(autoGenerate = true)
        val tagsPlaceHolder: Long,
        val created_at: String,
        val updated_at: String,
        val date: String?,
        val description: String?,
        val latitude: String?,
        val longitude: String?,
        val tag: String,
        val zoom_level: String?
)