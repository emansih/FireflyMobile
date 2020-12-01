package xyz.hisname.fireflyiii.repository.models.bills

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.time.LocalDate

@Entity(tableName = "billPayList")
data class BillPayDates(
        @PrimaryKey(autoGenerate = false)
        val billId: Long,
        val payDates: LocalDate
)