package xyz.hisname.fireflyiii.repository.models.budget.budgetList

import androidx.room.Embedded
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BudgetListModel(
        @Embedded
        val data: List<BudgetListData>,
        val meta: Meta
)