package xyz.hisname.fireflyiii.repository.models.budget.budgetList

import androidx.room.Entity
import androidx.room.Ignore

@Entity
data class BudgetListAttributes(
        var active: Boolean?,
        var created_at: String?,
        var name: String,
        var order: Int?,
        @Ignore
        var spent: List<Spent>?,
        var updated_at: String?
){
        constructor() : this(true,"","",1,null,"")
}