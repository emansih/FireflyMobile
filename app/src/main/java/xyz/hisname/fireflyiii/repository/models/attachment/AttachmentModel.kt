package xyz.hisname.fireflyiii.repository.models.attachment

import androidx.room.Embedded

data class AttachmentModel(
        @Embedded
        val data: MutableList<AttachmentData>
)