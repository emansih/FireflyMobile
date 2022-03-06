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

package xyz.hisname.fireflyiii.ui.transaction

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.databinding.RecentTransactionListBinding
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.getCompatColor
import xyz.hisname.fireflyiii.util.getUniqueHash

class TransactionAdapter(private val clickListener:(Transactions) -> Unit):
        PagingDataAdapter<Transactions, TransactionAdapter.TransactionViewHolder>(DIFF_CALLBACK) {
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        context = parent.context
        val itemView = RecentTransactionListBinding.inflate(LayoutInflater.from(context), parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int){
        getItem(position)?.let{
            holder.bind(it, clickListener)
        }
    }

    inner class TransactionViewHolder(
        private val view: RecentTransactionListBinding
    ): RecyclerView.ViewHolder(view.root) {
        fun bind(transactionAttributes: Transactions, clickListener: (Transactions) -> Unit){
            val sharedPref = context.getSharedPreferences(
                context.getUniqueHash().toString() + "-user-preferences", Context.MODE_PRIVATE)
            val timePreference = AppPref(sharedPref).dateTimeFormat
            val userDefinedDateTime = AppPref(sharedPref).userDefinedDateTimeFormat
            val transactionDescription = transactionAttributes.description
            val descriptionText = if(transactionAttributes.isPending){
                view.transactionNameText.setTextColor(context.getCompatColor(R.color.md_red_500))
                "$transactionDescription (Pending)"
            } else {
                transactionDescription
            }
            view.transactionNameText.text = descriptionText
            view.sourceNameText.text = transactionAttributes.source_name
            view.transactionJournalId.text = transactionAttributes.transaction_journal_id.toString()
            view.dateText.text = DateTimeUtil.convertLocalDateTime(transactionAttributes.date , timePreference, userDefinedDateTime)
            if(transactionAttributes.amount.toString().startsWith("-")){
                // Negative value means it's a withdrawal
                view.transactionAmountText.setTextColor(context.getCompatColor(R.color.md_red_500))
                view.transactionAmountText.text = "-" + transactionAttributes.currency_symbol +
                        Math.abs(transactionAttributes.amount)
            } else {
                view.transactionAmountText.text = transactionAttributes.currency_symbol +
                        transactionAttributes.amount.toString()
            }
            view.listItem.setOnClickListener {clickListener(transactionAttributes)}
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<Transactions>() {
            override fun areItemsTheSame(oldTransactions: Transactions,
                                         newTransactions: Transactions) =
                    oldTransactions == newTransactions

            override fun areContentsTheSame(oldTransactions: Transactions,
                                            newTransactions: Transactions) = oldTransactions == newTransactions
        }
    }
}