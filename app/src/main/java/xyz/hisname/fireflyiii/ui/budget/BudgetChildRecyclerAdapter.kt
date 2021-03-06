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

import android.animation.ObjectAnimator
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.RecyclerView
import xyz.hisname.fireflyiii.databinding.ChildBudgetListItemBinding
import xyz.hisname.fireflyiii.repository.models.budget.ChildIndividualBudget

class BudgetChildRecyclerAdapter(private val budgetData: List<ChildIndividualBudget>,
                                 private val clickListener:(ChildIndividualBudget) -> Unit):
        RecyclerView.Adapter<BudgetChildRecyclerAdapter.BudgetChildHolder>() {

    private var childBudgetListItemBinding: ChildBudgetListItemBinding? = null
    private val binding get() = childBudgetListItemBinding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetChildHolder {
        childBudgetListItemBinding = ChildBudgetListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BudgetChildHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetChildHolder, position: Int) {
        holder.bind(budgetData[position])
    }

    override fun getItemCount() = budgetData.size


    inner class BudgetChildHolder(itemView: ChildBudgetListItemBinding): RecyclerView.ViewHolder(itemView.root) {
        fun bind(budget: ChildIndividualBudget) {
            binding.userBudget.text = budget.currencySymbol + budget.budgetSpent + " / " +
                    budget.currencySymbol + budget.budgetAmount
            val progressDrawable = binding.budgetProgress.progressDrawable.mutate()
            val budgetPercentage = if(budget.budgetAmount == 0.toBigDecimal()){
                0
            } else {
                (budget.budgetSpent / budget.budgetAmount).multiply(100.toBigDecimal()).toInt()
            }
            when {
                budgetPercentage >= 80 -> {
                    progressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.RED,
                            BlendModeCompat.SRC_ATOP)
                }
                budgetPercentage in 50..80 -> {
                    progressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.YELLOW,
                            BlendModeCompat.SRC_ATOP)
                }
                else -> {
                    progressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.GREEN,
                            BlendModeCompat.SRC_ATOP)
                }
            }
            binding.budgetPercentage.text = "$budgetPercentage%"
            binding.budgetProgress.progressDrawable = progressDrawable
            ObjectAnimator.ofInt(binding.budgetProgress, "progress", budgetPercentage).start()
            binding.root.setOnClickListener { clickListener(budget) }
        }
    }
}