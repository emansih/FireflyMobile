package xyz.hisname.fireflyiii.repository.models.transaction

sealed class SplitSeparator{
    data class TransactionItem(val transaction: Transactions): SplitSeparator()
    data class SeparatorItem(val description: String) : SplitSeparator()
}