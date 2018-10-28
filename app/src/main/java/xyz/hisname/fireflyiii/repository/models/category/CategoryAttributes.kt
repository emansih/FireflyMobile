package xyz.hisname.fireflyiii.repository.models.category

import androidx.room.PrimaryKey

data class CategoryAttributes(
        @PrimaryKey(autoGenerate = true)
        val categoryIdPlaceHolder: Long,
        val created_at: String,
        val name: String,
        val updated_at: String
)