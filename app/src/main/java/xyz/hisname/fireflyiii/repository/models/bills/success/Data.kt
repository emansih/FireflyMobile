package xyz.hisname.fireflyiii.repository.models.bills.success

data class Data(
        val type: String,
        val id: String,
        val attributes: Attributes,
        val links: Links,
        val relationships: Relationships
)