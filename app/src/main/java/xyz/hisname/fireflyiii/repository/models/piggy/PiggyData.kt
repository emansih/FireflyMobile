package xyz.hisname.fireflyiii.repository.models.piggy

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "piggy")
data class PiggyData(
        @Ignore
        var type: String = "",
        @PrimaryKey(autoGenerate = false)
        @Json(name ="id")
        var piggyId: Long? = null,
        @Embedded
        @Json(name ="attributes")
        var piggyAttributes: PiggyAttributes? = null
)