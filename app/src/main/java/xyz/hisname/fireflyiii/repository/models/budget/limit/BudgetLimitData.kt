package xyz.hisname.fireflyiii.repository.models.budget.limit

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import xyz.hisname.fireflyiii.repository.models.accounts.Links

@Entity(tableName = "budget_limit")
data class BudgetLimitData(
        @Embedded
        @SerializedName("attributes")
        var limitAttributes: BudgetLimitAttributes? = null,
        @PrimaryKey(autoGenerate = false)
        @SerializedName("id")
        var budgetLimitId: Long? = null,
        @Ignore
        var links: Links? = null,
        @Ignore
        var type: String = "",
        @Embedded
        var relationships: Relationships? = null
)