package xyz.hisname.fireflyiii.repository.models.attachment

import androidx.room.Embedded
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AttachmentModel(
        @Embedded
        val data: MutableList<AttachmentData>
)