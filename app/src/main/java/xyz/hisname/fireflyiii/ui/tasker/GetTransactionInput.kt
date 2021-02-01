/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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