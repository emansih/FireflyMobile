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
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.TransactionCardDetailsBinding
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionAmountMonth
import xyz.hisname.fireflyiii.util.extension.getCompatColor

class TransactionMonthRecyclerView(private val items: List<TransactionAmountMonth>,
                                   private val clickListener:(Int) -> Unit):
        RecyclerView.Adapter<TransactionMonthRecyclerView.TransactionAdapter>() {

    private lateinit var context: Context
    private var transactionCardDetails: TransactionCardDetailsBinding? = null
    private val binding get() = transactionCardDetails!!


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionAdapter {
        context = parent.context
        return TransactionAdapter(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: TransactionAdapter, position: Int) = holder.bind(items[position], position)


    inner class TransactionAdapter(view: TransactionCardDetailsBinding): RecyclerView.ViewHolder(view.root) {
        fun bind(transactionData: TransactionAmountMonth, click: Int) {
            binding.transactionFreq.text = transactionData.transactionFreq.toString()
            binding.spentAmount.text = transactionData.transactionAmount
            binding.transactionCardDate.text = transactionData.monthYear
            if(transactionData.transactionType.contentEquals("Withdrawal")){
                binding.transactionTypeText.setText(R.string.spent)
                binding.spentAmount.setTextColor(context.getCompatColor(R.color.md_red_500))
            } else if(transactionData.transactionType.contentEquals("Deposit")){
                binding.transactionTypeText.setText(R.string.earned)
                binding.spentAmount.setTextColor(context.getCompatColor(R.color.md_green_500))
            } else {
                binding.transactionTypeText.setText(R.string.transfer)
                binding.spentAmount.setTextColor(context.getCompatColor(R.color.md_green_500))
            }
            binding.root.setOnClickListener { clickListener(click) }
        }
    }

}