package xyz.hisname.fireflyiii.repository.models.budget.limits

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "budgetLimit")
data class BudgetLimitData(
        @Embedded
        var attributes: BudgetLimitAttributes? = null,
        @PrimaryKey(autoGenerate = false)
        @SerializedName("id")
        var budgetLimitId: Long? = null,
        @Ignore
        var type: String? = ""
)