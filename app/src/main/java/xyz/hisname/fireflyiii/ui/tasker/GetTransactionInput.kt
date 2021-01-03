package xyz.hisname.fireflyiii.ui.tasker

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import xyz.hisname.fireflyiii.R

@TaskerInputRoot
class GetTransactionInput @JvmOverloads constructor(
        @field:TaskerInputField("transactionType", R.string.transactionType) var transactionType: String? = null,
        @field:TaskerInputField("transactionDescription", R.string.description) var transactionDescription: String? = null,
        @field:TaskerInputField("transactionAmount", R.string.amount) var transactionAmount: String? = null,
        @field:TaskerInputField("transactionDate", R.string.date) var transactionDate: String? = null,
        @field:TaskerInputField("transactionSourceAccount", R.string.source_account) var transactionSourceAccount: String? = null,
        @field:TaskerInputField("transactionDestinationAccount", R.string.destination_account) var transactionDestinationAccount: String? = null,
        @field:TaskerInputField("transactionPiggyBank", R.string.piggy_bank) var transactionPiggyBank: String? = null,
        @field:TaskerInputField("transactionTime", R.string.time) var transactionTime: String? = null,
        @field:TaskerInputField("transactionCurrency", R.string.currency_code) var transactionCurrency: String? = null,
        @field:TaskerInputField("transactionTags", R.string.tags) var transactionTags: String? = null,
        @field:TaskerInputField("transactionBudget", R.string.budget) var transactionBudget: String? = null,
        @field:TaskerInputField("transactionCategory", R.string.categories) var transactionCategory: String? = null,
        @field:TaskerInputField("transactionBill", R.string.bill) var transactionBill: String? = null,
        @field:TaskerInputField("transactionNotes", R.string.notes) var transactionNote: String? = null,
        @field:TaskerInputField("transactionUri", R.string.file) var transactionUri: String? = null
)