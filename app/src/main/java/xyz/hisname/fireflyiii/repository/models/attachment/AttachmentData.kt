package xyz.hisname.fireflyiii.repository.models.attachment

import androidx.room.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Entity(tableName = "attachment_info")
@JsonClass(generateAdapter = true)
data class AttachmentData(
        @Embedded
        @Json(name ="attributes")
        val attachmentAttributes: Attributes,
        @PrimaryKey(autoGenerate = false)
        @Json(name ="id")
        val attachmentId: Long
)