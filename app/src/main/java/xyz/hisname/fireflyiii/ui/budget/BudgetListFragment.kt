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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_budget_list.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getImprovedViewModel

class BudgetListFragment: BaseFragment(){

    private val budgetListViewModel by lazy { getImprovedViewModel(BudgetListViewModel::class.java) }
    private val isSummary by lazy { arguments?.getBoolean("isSummary") }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_budget_list,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWidget()
        activity?.activity_toolbar?.title = resources.getString(R.string.budget)
    }

    private fun setWidget(){
        if (isSummary != true){
            previousMonthArrow.setImageDrawable(IconicsDrawable(requireContext()).apply {
                icon = GoogleMaterial.Icon.gmd_keyboard_arrow_left
                sizeDp = 24
                colorRes = R.color.colorPrimary
            })
            nextMonthArrow.setImageDrawable(IconicsDrawable(requireContext()).apply {
                icon = GoogleMaterial.Icon.gmd_keyboard_arrow_right
                sizeDp = 24
                colorRes = R.color.colorPrimary
            })
            previousMonthArrow.setOnClickListener {
                budgetListViewModel.minusMonth()
            }
            nextMonthArrow.setOnClickListener {
                budgetListViewModel.addMonth()
            }
        }
        budgetListViewModel.spentValue.observe(viewLifecycleOwner){ spent ->
            spentAmount.text = spent
        }
        budgetListViewModel.budgetValue.observe(viewLifecycleOwner){ budgetValue ->
            budgetAmount.text = budgetValue
        }
        budgetListViewModel.budgetPercentage.observe(viewLifecycleOwner){ budgetPercentage ->
            val progressDrawable = budgetProgress.progressDrawable.mutate()
            when {
                budgetPercentage.toInt() >= 80 -> {
                    progressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.RED,
                            BlendModeCompat.SRC_ATOP)
                }
                budgetPercentage.toInt() in 50..80 -> {
                    progressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.YELLOW,
                            BlendModeCompat.SRC_ATOP)
                }
                else -> {
                    progressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.GREEN,
                            BlendModeCompat.SRC_ATOP)
                }
            }
            budgetProgress.progressDrawable = progressDrawable
            ObjectAnimator.ofInt(budgetProgress, "progress", budgetPercentage.toInt()).start()
        }
        setRecyclerView()
        budgetListViewModel.currencyName.observe(viewLifecycleOwner){ currency ->
            totalAvailableBudget.text = getString(R.string.total_available_in_currency, currency)
        }
        budgetListViewModel.displayMonth.observe(viewLifecycleOwner){ date ->
            monthAndYearText.text = date
        }
    }

    private fun setRecyclerView(){
        budgetListViewModel.individualBudget.observe(viewLifecycleOwner){ budgetData ->
            val budgetRecyclerAdapter = BudgetRecyclerAdapter(budgetData){ }
            budgetRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            budgetRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            budgetRecyclerView.adapter = budgetRecyclerAdapter
        }
    }

}