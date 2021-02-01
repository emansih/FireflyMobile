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

package xyz.hisname.fireflyiii.ui.budget

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.budget_list_item.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData
import xyz.hisname.fireflyiii.util.extension.inflate

class BudgetRecyclerAdapter(private val clickListener:(BudgetListData) -> Unit):
        PagingDataAdapter<BudgetListData, BudgetRecyclerAdapter.BudgetHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetHolder {
        return BudgetHolder(parent.inflate(R.layout.budget_list_item))
    }

    override fun onBindViewHolder(holder: BudgetHolder, position: Int){
        getItem(position)?.let{
            holder.bind(it, clickListener)
        }
    }

    inner class BudgetHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(budgetData: BudgetListData, clickListener: (BudgetListData) -> Unit) {
            val budgetAttributes = budgetData.budgetListAttributes
            itemView.budgetNameText.text = budgetAttributes.name
            itemView.setOnClickListener {clickListener(budgetData)}
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<BudgetListData>() {
            override fun areItemsTheSame(oldBudgetListData: BudgetListData,
                                         newBudgetListData: BudgetListData) = oldBudgetListData == newBudgetListData

            override fun areContentsTheSame(oldBudgetListData: BudgetListData,
                                            newBudgetListData: BudgetListData) = oldBudgetListData == newBudgetListData
        }
    }
}