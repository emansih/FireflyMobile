package xyz.hisname.fireflyiii.repository.models.category

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "category")
data class CategoryData(
        @PrimaryKey(autoGenerate = false)
        @SerializedName("id")
        var id: String = "",
        @Embedded
        @SerializedName("attributes")
        var categoryAttributes: CategoryAttributes? = null,
        @Ignore
        val type: String = ""
)