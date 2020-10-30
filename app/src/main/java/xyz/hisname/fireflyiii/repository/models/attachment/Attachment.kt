package xyz.hisname.fireflyiii.repository.models.attachment

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Attachment(
        val data: AttachmentData
)