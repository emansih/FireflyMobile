package xyz.hisname.fireflyiii.repository.models.transaction.sucess

data class Data(
        val type: String,
        val id: String,
        val attributes: Attributes,
        val links: Links
)