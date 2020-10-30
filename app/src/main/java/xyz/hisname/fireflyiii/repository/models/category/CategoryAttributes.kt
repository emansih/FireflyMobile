package xyz.hisname.fireflyiii.repository.models.category

import androidx.room.Entity
import com.squareup.moshi.JsonClass

@Entity
@JsonClass(generateAdapter = true)
data class CategoryAttributes(
        val created_at: String?,
        val name: String,
        val updated_at: String?
)