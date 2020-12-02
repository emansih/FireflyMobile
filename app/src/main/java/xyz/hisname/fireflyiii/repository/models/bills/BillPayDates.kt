package xyz.hisname.fireflyiii.repository.models.bills

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "billPayList", foreignKeys = [ForeignKey(entity = BillData::class,
        parentColumns = arrayOf("billId"), childColumns = arrayOf("id"),
        onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE, deferred = true)],
        indices = [Index("id")])
data class BillPayDates(
        @PrimaryKey(autoGenerate = true)
        val billPayPrimaryKey: Long = 0,
        val id: Long,
        val payDates: LocalDate
)