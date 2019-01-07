package xyz.hisname.fireflyiii.repository.models.budget.budgetList

import androidx.room.Embedded

data class BudgetListModel(
        @Embedded
        val data: List<BudgetListData>,
        val meta: Meta
)