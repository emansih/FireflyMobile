package xyz.hisname.fireflyiii.repository.models.budget.budget

import androidx.room.PrimaryKey

data class BudgetAttributes(
        @PrimaryKey(autoGenerate = true)
        val budgetIdPlaceHolder: Long,
        val active: Boolean,
        val created_at: String,
        val name: String,
        val updated_at: String
)