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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.budget_list_item.view.*
import kotlinx.android.synthetic.main.fragment_budget_list.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getImprovedViewModel

class BudgetListFragment: BaseFragment(){

    private val budgetListViewModel by lazy { getImprovedViewModel(BudgetListViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_budget_list, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.activity_toolbar?.title = resources.getString(R.string.budget)
        setWidget()
        budgetListViewModel.apiResponse.observe(viewLifecycleOwner){
            toastError(it)
        }
        budgetListViewModel.isLoading.observe(viewLifecycleOwner){ loading ->
            swipeContainer.isRefreshing = loading
        }
        swipeContainer.setOnRefreshListener {
            budgetListViewModel.setDisplayDate()
        }
    }

    private fun setWidget(){
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

        zipLiveData(budgetListViewModel.spentValue, budgetListViewModel.budgetValue).observe(viewLifecycleOwner){ money ->
            userBudget.text = money.first + " / " + money.second
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
            userBudgetPercentage.text = budgetPercentage.toString() + "%"
            ObjectAnimator.ofInt(budgetProgress, "progress", budgetPercentage.toInt()).start()
        }
        setRecyclerView()
        zipLiveData(budgetListViewModel.currencyName,
                budgetListViewModel.displayMonth).observe(viewLifecycleOwner){ data ->
            totalAvailableBudget.text = getString(R.string.total_available_in_currency, data.first)
            monthAndYearText.text = data.second
            availableBudget.setOnClickListener {
                val layoutInflater = LayoutInflater.from(requireContext())
                val availableLayout = layoutInflater.inflate(R.layout.available_budget_layout, null)
                val input = availableLayout.findViewById<TextInputEditText>(R.id.availableBudgetEditText)
                AlertDialog.Builder(requireContext())
                        .setTitle(data.second)
                        .setMessage(getString(R.string.expected_budget, data.first))
                        .setView(availableLayout)
                        .setPositiveButton(android.R.string.ok) { _,_ ->
                            budgetListViewModel.setBudget(input.getString(), data.first)
                        }
                        .show()
            }
        }
        initFab()
    }

    private fun initFab(){
        extendedFab.display {
            extendedFab.isClickable = false
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, AddBudgetFragment().apply {
                    arguments = bundleOf("revealX" to extendedFab.width / 2, "revealY" to extendedFab.height / 2)
                })
                addToBackStack(null)
            }
            extendedFab.isClickable = true
        }
    }

    private fun setRecyclerView(){
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        budgetListViewModel.individualBudget.observe(viewLifecycleOwner){ budgetData ->
            val budgetRecyclerAdapter = BudgetRecyclerAdapter(budgetData){ cid ->
                
            }
            recycler_view.adapter = budgetRecyclerAdapter
        }
        recycler_view.enableDragDrop(extendedFab) { viewHolder, isCurrentlyActive ->
            if (viewHolder.itemView.budgetItemList.isOverlapping(extendedFab)) {
                extendedFab.dropToRemove()
                if (!isCurrentlyActive) {
                    val budgetName = viewHolder.itemView.budgetNameText.text.toString()
                    budgetListViewModel.deleteBudget(viewHolder.itemView.budgetNameText.text.toString()).observe(viewLifecycleOwner){ isDeleted ->
                        if(!isDeleted){
                            toastOffline("Error deleting $budgetName", Toast.LENGTH_LONG)
                        } else {
                            budgetListViewModel.setDisplayDate()
                        }
                    }
                }
            }
        }
        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && extendedFab.isShown) {
                    extendedFab.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    extendedFab.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

}