package xyz.hisname.fireflyiii.repository.models.transaction

data class TransactionData(
        val type: String,
        val id: String,
        val attributes: Attributes
)