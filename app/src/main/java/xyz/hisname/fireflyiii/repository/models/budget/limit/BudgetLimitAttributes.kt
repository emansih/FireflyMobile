package xyz.hisname.fireflyiii.repository.models.budget.limit

import androidx.room.PrimaryKey

data class BudgetLimitAttributes(
        @PrimaryKey(autoGenerate = true)
        val budgetIdPlaceHolder: Long,
        val amount: Double,
        val created_at: String,
        val end_date: String,
        val start_date: String,
        val updated_at: String
)