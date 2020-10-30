package xyz.hisname.fireflyiii.repository.models.budget.budgetList

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Meta(
        val pagination: Pagination
)