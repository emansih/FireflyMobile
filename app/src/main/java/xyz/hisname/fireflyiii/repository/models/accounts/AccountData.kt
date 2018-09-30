package xyz.hisname.fireflyiii.repository.models.accounts

data class AccountData(
        val type: String,
        val id: String,
        val attributes: Attributes,
        val links: Links
)