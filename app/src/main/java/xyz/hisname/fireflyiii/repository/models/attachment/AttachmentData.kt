package xyz.hisname.fireflyiii.repository.models.attachment

import androidx.room.*
import com.google.gson.annotations.SerializedName

@Entity(tableName = "attachment_info")
data class AttachmentData(
        @Embedded
        @SerializedName("attributes")
        var attachmentAttributes: Attributes = Attributes(0,"","","","","","","",0,"","",""),
        @PrimaryKey(autoGenerate = false)
        @SerializedName("id")
        var attachmentId: Long = 0,
        @Ignore
        var type: String = ""
)