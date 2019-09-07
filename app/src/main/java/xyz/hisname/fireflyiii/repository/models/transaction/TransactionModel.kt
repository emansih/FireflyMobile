package xyz.hisname.fireflyiii.repository.models.transaction


data class TransactionModel(
        val data: MutableCollection<TransactionData>,
        val meta: Meta
)