package xyz.hisname.fireflyiii.repository.models.rules

data class RulesData(
        val type: String,
        val id: String,
        val attributes: Attributes,
        val links: LinksX,
        val relationships: Relationships
)