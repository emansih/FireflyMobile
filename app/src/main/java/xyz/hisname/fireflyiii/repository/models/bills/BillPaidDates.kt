package xyz.hisname.fireflyiii.repository.models.bills

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.time.LocalDate

@JsonClass(generateAdapter = true)
@Entity(tableName = "billPaidList",
        foreignKeys = [ForeignKey(entity = BillData::class,
                parentColumns = arrayOf("billId"), childColumns = arrayOf("id"),
                onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)])
data class BillPaidDates(
        @PrimaryKey(autoGenerate = true)
        val billPaidPrimaryKey: Long = 0L,
        val id: Long = 0L,
        val transaction_group_id: Long,
        val transaction_journal_id: Long,
        val date: LocalDate
)