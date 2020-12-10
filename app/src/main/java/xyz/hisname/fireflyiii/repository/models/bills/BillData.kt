package xyz.hisname.fireflyiii.repository.models.bills

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "bills")
data class BillData(
        @PrimaryKey(autoGenerate = false)
        @Json(name ="id")
        var billId: Long = 0,
        @Embedded
        @Json(name ="attributes")
        var billAttributes: BillAttributes? = null
)
