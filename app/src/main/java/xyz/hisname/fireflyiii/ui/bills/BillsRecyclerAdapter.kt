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

package xyz.hisname.fireflyiii.ui.bills

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bills_list_item.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.util.extension.getCompatColor
import xyz.hisname.fireflyiii.util.extension.inflate

class BillsRecyclerAdapter(private val clickListener:(BillData) -> Unit):
        PagingDataAdapter<BillData, BillsRecyclerAdapter.BillsHolder>(DIFF_CALLBACK) {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillsHolder {
        context = parent.context
        return BillsHolder(parent.inflate(R.layout.bills_list_item))
    }

    override fun onBindViewHolder(holder: BillsHolder, position: Int) {
        getItem(position)?.let{
            holder.bind(it, clickListener)
        }
    }

    inner class BillsHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(billData: BillData, clickListener: (BillData) -> Unit){
            val billResponse = billData.billAttributes
            var billName = billResponse.name
            val isPending = billResponse.isPending
            if(isPending){
                billName = "$billName (Pending)"
                itemView.billName.setTextColor(context.getCompatColor(R.color.md_red_500))
            }
            itemView.billName.text = billName
            itemView.billAmount.text = context.getString(R.string.bill_amount,
                    billResponse.currency_symbol, billResponse.amount_max)
            val freq = billResponse.repeat_freq
            itemView.billFreq.text = freq.substring(0,1).toUpperCase() + freq.substring(1)

            val nextMatch = billResponse.next_expected_match
            if(nextMatch != null){
                itemView.billNextDueDate.text = billResponse.next_expected_match
            }
            itemView.billId.text = billData.billId.toString()
            itemView.setOnClickListener{clickListener(billData)}
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<BillData>() {
            override fun areItemsTheSame(oldBill: BillData,
                                         newBill: BillData) = oldBill == newBill

            override fun areContentsTheSame(oldBill: BillData,
                                            newBill: BillData) = oldBill == newBill
        }
    }
}
