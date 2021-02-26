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
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bills_to_pay_item.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.util.extension.inflate
import java.math.BigDecimal

class BillsToPayRecyclerView(private val budgetData: List<BillData>,
                             private val clickListener:(BillData) -> Unit):
        RecyclerView.Adapter<BillsToPayRecyclerView.BillsToPayHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillsToPayHolder {
        context = parent.context
        return BillsToPayHolder(parent.inflate(R.layout.bills_to_pay_item))
    }

    override fun onBindViewHolder(holder: BillsToPayHolder, position: Int) {
        holder.bind(budgetData[position], clickListener)
    }

    override fun getItemCount() = budgetData.size

    inner class BillsToPayHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(billData: BillData, clickListener: (BillData) -> Unit){
            val billResponse = billData.billAttributes
            val billName = billResponse.name
            val amountToDisplay = billResponse.amount_max
                    .plus(billResponse.amount_min)
                    .div(BigDecimal.valueOf(2))
            itemView.billName.text = billName
            itemView.billAmount.text = context.getString(R.string.bill_amount,
                    billResponse.currency_symbol, amountToDisplay)
            itemView.setOnClickListener{clickListener(billData)}
        }
    }
}