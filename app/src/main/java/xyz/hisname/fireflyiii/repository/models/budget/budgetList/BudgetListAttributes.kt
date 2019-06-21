package xyz.hisname.fireflyiii.repository.models.budget.budgetList

import androidx.room.Entity

@Entity
data class BudgetListAttributes(
        val active: Boolean?,
        val created_at: String?,
        val name: String,
        val order: Int?,
        val spent: List<Spent>?,
        val updated_at: String?
)