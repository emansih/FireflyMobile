package xyz.hisname.fireflyiii.repository.models.transaction

sealed class SplitSeparator{
    data class TransactionItem(val transaction: TransactionList): SplitSeparator()
    data class SeparatorItem(val description: String) : SplitSeparator()
}