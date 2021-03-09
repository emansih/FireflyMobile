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
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.databinding.RecentTransactionListBinding
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.getCompatColor

class TransactionAdapter(private val clickListener:(Transactions) -> Unit):
        PagingDataAdapter<Transactions, TransactionAdapter.TransactionViewHolder>(DIFF_CALLBACK) {

    private lateinit var context: Context
    private var recentTransactionListBinding: RecentTransactionListBinding? = null
    private val binding get() = recentTransactionListBinding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        context = parent.context
        recentTransactionListBinding = RecentTransactionListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int){
        getItem(position)?.let{
            holder.bind(it, clickListener)
        }
    }

    inner class TransactionViewHolder(view: RecentTransactionListBinding): RecyclerView.ViewHolder(view.root) {
        fun bind(transactionAttributes: Transactions, clickListener: (Transactions) -> Unit){
            val timePreference = AppPref(PreferenceManager.getDefaultSharedPreferences(context)).dateTimeFormat
            val userDefinedDateTime = AppPref(PreferenceManager.getDefaultSharedPreferences(context)).userDefinedDateTimeFormat
            val transactionDescription = transactionAttributes.description
            val descriptionText = if(transactionAttributes.isPending){
                binding.transactionNameText.setTextColor(context.getCompatColor(R.color.md_red_500))
                "$transactionDescription (Pending)"
            } else {
                transactionDescription
            }
            binding.transactionNameText.text = descriptionText
            binding.sourceNameText.text = transactionAttributes.source_name
            binding.transactionJournalId.text = transactionAttributes.transaction_journal_id.toString()
            binding.dateText.text = DateTimeUtil.convertLocalDateTime(transactionAttributes.date , timePreference, userDefinedDateTime)
            if(transactionAttributes.amount.toString().startsWith("-")){
                // Negative value means it's a withdrawal
                binding.transactionAmountText.setTextColor(context.getCompatColor(R.color.md_red_500))
                binding.transactionAmountText.text = "-" + transactionAttributes.currency_symbol +
                        Math.abs(transactionAttributes.amount)
            } else {
                binding.transactionAmountText.text = transactionAttributes.currency_symbol +
                        transactionAttributes.amount.toString()
            }
            binding.listItem.setOnClickListener {clickListener(transactionAttributes)}
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