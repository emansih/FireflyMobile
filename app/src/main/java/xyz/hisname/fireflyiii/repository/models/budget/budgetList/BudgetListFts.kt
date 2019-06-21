package xyz.hisname.fireflyiii.repository.models.budget.budgetList

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = BudgetListData::class)
@Entity(tableName = "budgetListFts")
data class BudgetListFts(
        val name: String,
        val budgetListId: String
)