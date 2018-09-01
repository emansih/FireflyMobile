package xyz.hisname.fireflyiii.repository.models.transaction

data class TransactionModel(
        val data: MutableCollection<Data>,
        val meta: Meta,
        val links: Links
)