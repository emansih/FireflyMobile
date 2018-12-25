package xyz.hisname.fireflyiii.repository.models.tags

import androidx.room.Entity

@Entity
data class TagsAttributes(
        val created_at: String,
        val updated_at: String,
        val date: String?,
        val description: String?,
        val latitude: String?,
        val longitude: String?,
        val tag: String,
        val zoom_level: String?
)