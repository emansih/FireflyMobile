package xyz.hisname.fireflyiii.repository.models.budget.limits

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "budgetLimit")
data class BudgetLimitData(
        @Embedded
        val attributes: BudgetLimitAttributes,
        @PrimaryKey(autoGenerate = false)
        @Json(name ="id")
        val budgetLimitId: Long
)