package xyz.hisname.fireflyiii.repository.models.piggy

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "piggy")
data class PiggyData(
        @PrimaryKey(autoGenerate = false)
        @Json(name ="id")
        val piggyId: Long,
        @Embedded
        @Json(name ="attributes")
        val piggyAttributes: PiggyAttributes
)