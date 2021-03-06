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
import xyz.hisname.fireflyiii.databinding.TransactionItemSeparatorBinding
import xyz.hisname.fireflyiii.repository.models.transaction.SplitSeparator
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.getCompatColor
import kotlin.math.abs

class TransactionSeparatorAdapter(private val clickListener:(Transactions) -> Unit):
        PagingDataAdapter<SplitSeparator, RecyclerView.ViewHolder>(DIFF_CALLBACK){

    private lateinit var context: Context
    private var recentTransactionListBinding: RecentTransactionListBinding? = null
    private val binding get() = recentTransactionListBinding!!
    private var transactionItemSeparatorBinding: TransactionItemSeparatorBinding? = null
    private val separatorBinding get() = transactionItemSeparatorBinding!!


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return when(viewType){
            R.layout.recent_transaction_list -> {
                recentTransactionListBinding = RecentTransactionListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TransactionViewHolder(binding)
            }
            else -> {
                transactionItemSeparatorBinding = TransactionItemSeparatorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SplitSeparatorViewHolder(separatorBinding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is SplitSeparator.TransactionItem -> R.layout.recent_transaction_list
            is SplitSeparator.SeparatorItem -> R.layout.transaction_item_separator
            null -> throw UnsupportedOperationException("Unknown view")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int){
        val transactionModel = getItem(position)
        transactionModel.let { separator ->
            when(separator){
                is SplitSeparator.SeparatorItem -> {
                    separatorBinding.separatorDescription.text = separator.description
                }
                is SplitSeparator.TransactionItem -> {
                    val viewHolder = holder as TransactionViewHolder
                    val timePreference = AppPref(PreferenceManager.getDefaultSharedPreferences(context)).timeFormat
                    val transactionDescription = separator.transaction.description
                    val descriptionText = if(separator.transaction.isPending){
                        binding.transactionNameText.setTextColor(context.getCompatColor(R.color.md_red_500))
                        "$transactionDescription (Pending)"
                    } else {
                        transactionDescription
                    }
                    binding.transactionNameText.text = descriptionText
                    binding.sourceNameText.text = separator.transaction.source_name
                    binding.transactionJournalId.text = separator.transaction.transaction_journal_id.toString()
                    binding.dateText.text = DateTimeUtil.convertLocalDateTime(separator.transaction.date,
                            timePreference)
                    if(separator.transaction.amount.toString().startsWith("-")){
                        // Negative value means it's a withdrawal
                        binding.transactionAmountText.setTextColor(context.getCompatColor(R.color.md_red_500))
                        binding.transactionAmountText.text = "-" + separator.transaction.currency_symbol +
                                abs(separator.transaction.amount)
                    } else {
                        binding.transactionAmountText.text = separator.transaction.currency_symbol +
                                separator.transaction.amount.toString()
                    }
                    binding.listItem.setOnClickListener {clickListener(separator.transaction)}
                }
            }
        }
    }


    class TransactionViewHolder(view: RecentTransactionListBinding) : RecyclerView.ViewHolder(view.root)
    class SplitSeparatorViewHolder(view: TransactionItemSeparatorBinding) : RecyclerView.ViewHolder(view.root)


    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<SplitSeparator>() {
            override fun areItemsTheSame(oldTransactions: SplitSeparator,
                                         newTransactions: SplitSeparator) =
                    oldTransactions == newTransactions

            override fun areContentsTheSame(oldTransactions: SplitSeparator,
                                            newTransactions: SplitSeparator) = oldTransactions == newTransactions
        }
    }
}