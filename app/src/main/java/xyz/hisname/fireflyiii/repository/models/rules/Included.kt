package xyz.hisname.fireflyiii.repository.models.rules

data class Included(
        val type: String,
        val id: String,
        val attributes: AttributesX,
        val links: Links,
        val relationships: RelationshipsX
)