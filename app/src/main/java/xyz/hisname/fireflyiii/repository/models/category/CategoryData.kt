package xyz.hisname.fireflyiii.repository.models.category

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "category")
data class CategoryData(
        @SerializedName("attributes")
        var categoryAttributes: CategoryAttributes? = null,
        @PrimaryKey(autoGenerate = false)
        @SerializedName("id")
        var id: String = "",
        @Ignore
        val type: String = ""
)