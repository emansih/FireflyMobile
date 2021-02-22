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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.budget_list_item.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.budget.ChildIndividualBudget
import xyz.hisname.fireflyiii.repository.models.budget.IndividualBudget
import xyz.hisname.fireflyiii.util.extension.inflate

class BudgetRecyclerAdapter(private val budgetData: List<IndividualBudget>,
                            private val clickListener:(ChildIndividualBudget) -> Unit):
        RecyclerView.Adapter<BudgetRecyclerAdapter.BudgetHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetHolder {
        return BudgetHolder(parent.inflate(R.layout.budget_list_item))
    }

    override fun getItemCount() = budgetData.size

    override fun onBindViewHolder(holder: BudgetHolder, position: Int){
        if(budgetData.isNotEmpty()){
            holder.bind(budgetData[position])
            val childRecyclerView = holder.itemView.budgetChildRecyclerView
            val layoutManager = LinearLayoutManager(childRecyclerView.context)
            val childItemAdapter = BudgetChildRecyclerAdapter(budgetData[position].listOfChildIndividualBudget, clickListener)
            if(budgetData[position].listOfChildIndividualBudget.size > 1){
                childRecyclerView.addItemDecoration(DividerItemDecoration(childRecyclerView.context, DividerItemDecoration.VERTICAL))
            }
            childRecyclerView.layoutManager = layoutManager
            childRecyclerView.adapter = childItemAdapter
        }
    }

    inner class BudgetHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(budget: IndividualBudget) {
            itemView.budgetNameText.text = budget.budgetName
        }
    }
}