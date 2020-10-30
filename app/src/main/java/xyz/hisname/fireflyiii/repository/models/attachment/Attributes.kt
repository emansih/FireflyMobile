package xyz.hisname.fireflyiii.repository.models.attachment

import androidx.room.Entity
import com.squareup.moshi.JsonClass

@Entity
@JsonClass(generateAdapter = true)
data class Attributes(
        val attachable_id: Int,
        val attachable_type: String,
        val created_at: String,
        val download_uri: String,
        val filename: String,
        val md5: String,
        val mime: String,
        val notes: String,
        val size: Int,
        val title: String,
        val updated_at: String,
        val upload_uri: String
)