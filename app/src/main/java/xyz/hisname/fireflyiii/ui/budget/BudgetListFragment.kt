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
import androidx.appcompat.widget.Toolbar
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
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.BaseSwipeLayoutBinding
import xyz.hisname.fireflyiii.databinding.BudgetListItemBinding
import xyz.hisname.fireflyiii.databinding.FragmentBudgetListBinding
import xyz.hisname.fireflyiii.repository.models.budget.ChildIndividualBudget
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getImprovedViewModel

class BudgetListFragment: BaseFragment(){

    private val budgetListViewModel by lazy { getImprovedViewModel(BudgetListViewModel::class.java) }
    private var fragmentBudgetListBinding: FragmentBudgetListBinding? = null
    private val binding get() = fragmentBudgetListBinding!!
    private var baseSwipeLayout: BaseSwipeLayoutBinding? = null
    private val baseSwipeBinding get() = baseSwipeLayout!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentBudgetListBinding = FragmentBudgetListBinding.inflate(inflater, container, false)
        baseSwipeLayout = binding.baseLayout.baseSwipeLayout
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = resources.getString(R.string.budget)
        setWidget()
        budgetListViewModel.apiResponse.observe(viewLifecycleOwner){
            toastError(it)
        }
        budgetListViewModel.isLoading.observe(viewLifecycleOwner){ loading ->
            baseSwipeBinding.swipeContainer.isRefreshing = loading
        }
        baseSwipeBinding.swipeContainer.setOnRefreshListener {
            budgetListViewModel.setDisplayDate()
        }
    }

    private fun setWidget(){
        binding.previousMonthArrow.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = GoogleMaterial.Icon.gmd_keyboard_arrow_left
            sizeDp = 24
            colorRes = R.color.colorPrimary
        })

        binding.nextMonthArrow.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = GoogleMaterial.Icon.gmd_keyboard_arrow_right
            sizeDp = 24
            colorRes = R.color.colorPrimary
        })

        binding.previousMonthArrow.setOnClickListener {
            budgetListViewModel.minusMonth()
        }
        binding.nextMonthArrow.setOnClickListener {
            budgetListViewModel.addMonth()
        }

        zipLiveData(budgetListViewModel.spentValue, budgetListViewModel.budgetValue).observe(viewLifecycleOwner){ money ->
            binding.userBudget.text = money.first + " / " + money.second
        }

        budgetListViewModel.budgetPercentage.observe(viewLifecycleOwner){ budgetPercentage ->
            val progressDrawable = binding.budgetProgress.progressDrawable.mutate()
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
            binding.budgetProgress.progressDrawable = progressDrawable
            binding.userBudgetPercentage.text = budgetPercentage.toString() + "%"
            ObjectAnimator.ofInt(binding.budgetProgress, "progress", budgetPercentage.toInt()).start()
        }
        setRecyclerView()
        zipLiveData(budgetListViewModel.currencyName,
                budgetListViewModel.displayMonth).observe(viewLifecycleOwner){ data ->
            binding.totalAvailableBudget.text = getString(R.string.total_available_in_currency, data.first)
            binding.monthAndYearText.text = data.second
            binding.availableBudget.setOnClickListener {
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
        baseSwipeBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        budgetListViewModel.individualBudget.observe(viewLifecycleOwner){ budgetData ->
            val budgetRecyclerAdapter = BudgetRecyclerAdapter(budgetData, { child: ChildIndividualBudget ->
                parentFragmentManager.commit {
                    replace(R.id.bigger_fragment_container, AddBudgetFragment().apply {
                        arguments = bundleOf("budgetId" to child.budgetLimitId,
                            "currencySymbol" to child.currencySymbol)
                    })
                    addToBackStack(null)
                }
            }){ adult: Long ->
                parentFragmentManager.commit {
                    replace(R.id.bigger_fragment_container, AddBudgetFragment().apply {
                        arguments = bundleOf("budgetId" to adult)
                    })
                    addToBackStack(null)
                }
            }
            baseSwipeBinding.recyclerView.adapter = budgetRecyclerAdapter
        }
        baseSwipeBinding.recyclerView.enableDragDrop(extendedFab) { viewHolder, isCurrentlyActive ->
            val budgetItemListBinding = BudgetListItemBinding.bind(viewHolder.itemView)
            if (budgetItemListBinding.budgetItemList.isOverlapping(extendedFab)) {
                extendedFab.dropToRemove()
                if (!isCurrentlyActive) {
                    val budgetName = budgetItemListBinding.budgetNameText.text.toString()
                    budgetListViewModel.deleteBudget(budgetItemListBinding.budgetNameText.text.toString()).observe(viewLifecycleOwner){ isDeleted ->
                        if(!isDeleted){
                            toastOffline("Error deleting $budgetName", Toast.LENGTH_LONG)
                        } else {
                            budgetListViewModel.setDisplayDate()
                        }
                    }
                }
            }
        }
        baseSwipeBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

    override fun onDestroyView() {
        super.onDestroyView()
        baseSwipeLayout = null
        fragmentBudgetListBinding = null
    }
}