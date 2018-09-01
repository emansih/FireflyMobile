package xyz.hisname.fireflyiii.repository.models.bills

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "bills")
data class BillData(
        @Ignore
        var type: String = "",
        @PrimaryKey(autoGenerate = false)
        @SerializedName("id")
        var billId: Long? = null,
        @Embedded
        @SerializedName("attributes")
        var billAttributes: BillAttributes? = null,
        @Ignore
        var relationships: Relationships? = null
)
