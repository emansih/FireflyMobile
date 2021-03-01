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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import xyz.hisname.fireflyiii.databinding.BudgetListItemBinding
import xyz.hisname.fireflyiii.repository.models.budget.ChildIndividualBudget
import xyz.hisname.fireflyiii.repository.models.budget.IndividualBudget

class BudgetRecyclerAdapter(private val budgetData: List<IndividualBudget>,
                            private val clickListener:(ChildIndividualBudget) -> Unit):
        RecyclerView.Adapter<BudgetRecyclerAdapter.BudgetHolder>() {

    private var budgetListItemBinding: BudgetListItemBinding? = null
    private val binding get() = budgetListItemBinding!!


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetHolder {
        budgetListItemBinding = BudgetListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BudgetHolder(binding)
    }

    override fun getItemCount() = budgetData.size

    override fun onBindViewHolder(holder: BudgetHolder, position: Int){
        if(budgetData.isNotEmpty()){
            holder.bind(budgetData[position])
            val childRecyclerView = binding.budgetChildRecyclerView
            val layoutManager = LinearLayoutManager(childRecyclerView.context)
            val childItemAdapter = BudgetChildRecyclerAdapter(budgetData[position].listOfChildIndividualBudget, clickListener)
            if(budgetData[position].listOfChildIndividualBudget.size > 1){
                childRecyclerView.addItemDecoration(DividerItemDecoration(childRecyclerView.context, DividerItemDecoration.VERTICAL))
            }
            childRecyclerView.layoutManager = layoutManager
            childRecyclerView.adapter = childItemAdapter
        }
    }

    inner class BudgetHolder(itemView: BudgetListItemBinding): RecyclerView.ViewHolder(itemView.root) {
        fun bind(budget: IndividualBudget) {
            binding.budgetNameText.text = budget.budgetName
        }
    }
}