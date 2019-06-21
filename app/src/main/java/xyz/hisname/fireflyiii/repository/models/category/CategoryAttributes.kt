package xyz.hisname.fireflyiii.repository.models.category

import androidx.room.Entity

@Entity
data class CategoryAttributes(
        val created_at: String?,
        val name: String,
        val updated_at: String?
)