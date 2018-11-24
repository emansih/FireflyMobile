package xyz.hisname.fireflyiii.repository.models.budget.budget

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "budget")
data class BudgetData(
        @Embedded
        @SerializedName("attributes")
        var budgetAttributes: BudgetAttributes? = null,
        @PrimaryKey(autoGenerate = false)
        @SerializedName("id")
        var budgetId: Long? = null,
        @Ignore
        var type: String = ""
)