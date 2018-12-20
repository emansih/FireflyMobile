package xyz.hisname.fireflyiii.repository.models.tags

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tags")
data class TagsData(
        @Embedded
        @SerializedName("attributes")
        var tagsAttributes: TagsAttributes? = null,
        @SerializedName("id")
        @PrimaryKey(autoGenerate = false)
        var tagsId: Long? = null,
        @Ignore
        var type: String =""
)