package xyz.hisname.fireflyiii.repository.models.budget.budgetList

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "budget_list")
data class BudgetListData(
        @Embedded
        @SerializedName("attributes")
        var budgetListAttributes: BudgetListAttributes? = null,
        @PrimaryKey(autoGenerate = false)
        @SerializedName("id")
        var budgetListId: Long? = null,
        @Ignore
        var type: String = ""
)