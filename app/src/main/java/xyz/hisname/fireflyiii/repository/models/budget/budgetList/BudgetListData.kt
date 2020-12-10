package xyz.hisname.fireflyiii.repository.models.budget.budgetList

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "budget_list")
data class BudgetListData(
        @PrimaryKey(autoGenerate = false)
        @Json(name ="id")
        val budgetListId: Long,
        @Embedded
        @Json(name ="attributes")
        val budgetListAttributes: BudgetListAttributes
)