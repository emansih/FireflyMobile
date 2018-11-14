package xyz.hisname.fireflyiii.repository.models.category

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CategoryAttributes(
        @PrimaryKey(autoGenerate = true)
        val categoryIdPlaceHolder: Long,
        val created_at: String,
        val name: String,
        val updated_at: String
)