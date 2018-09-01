package xyz.hisname.fireflyiii.repository.models.piggy

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "piggy")
data class PiggyData(
        @Ignore
        var type: String = "",
        @PrimaryKey(autoGenerate = false)
        @SerializedName("id")
        var piggyId: Long? = null,
        @Embedded
        @SerializedName("attributes")
        var piggyAttributes: PiggyAttributes? = null,
        @Ignore
        var links: LinksX? = null
)