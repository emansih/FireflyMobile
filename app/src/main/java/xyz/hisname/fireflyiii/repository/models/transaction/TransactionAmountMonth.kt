package xyz.hisname.fireflyiii.repository.models.transaction

data class TransactionAmountMonth(
        val monthYear: String,
        val transactionAmount: String,
        val transactionFreq: Int,
        val transactionType: String
)