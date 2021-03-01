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

package xyz.hisname.fireflyiii.ui.categories

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.FragmentCategoryDetailBinding
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.ui.transaction.TransactionSeparatorAdapter
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getImprovedViewModel

class CategoryDetailsFragment: BaseDetailFragment() {

    private val categoryDetailViewModel by lazy { getImprovedViewModel(CategoryDetailViewModel::class.java) }
    private lateinit var categoryName: String
    private val categoryId by lazy { arguments?.getLong("categoryId")  ?: 0 }
    private var fragmentCategoryDetailBinding: FragmentCategoryDetailBinding? = null
    private val binding get() = fragmentCategoryDetailBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentCategoryDetailBinding = FragmentCategoryDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWidget()
    }

    private fun setWidget(){
        categoryDetailViewModel.getCategoryById(categoryId).observe(viewLifecycleOwner){ categoryData ->
            categoryName = categoryData.categoryAttributes.name
            binding.durationText.text = getString(R.string.chart_category_in_period, categoryName,
                    DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth())
            if(categoryData.categoryAttributes.notes.isNullOrEmpty()){
                binding.notesCard.isGone = true
            } else {
                binding.notesText.text = categoryData.categoryAttributes.notes.toMarkDown()
            }
        }
        setBarChart()
        loadTransactionList()
    }

    private fun setBarChart(){
        val withdrawCategory = arrayListOf<BarEntry>()
        val depositCategory = arrayListOf<BarEntry>()
        zipLiveData(categoryDetailViewModel.withdrawData,
                categoryDetailViewModel.depositData).observe(viewLifecycleOwner){ categoryData ->
            categoryData.first.forEachIndexed { index, entry ->
                withdrawCategory.add(BarEntry(index.toFloat(), entry))
            }
            categoryData.second.forEachIndexed { index, entry ->
                depositCategory.add(BarEntry(index.toFloat(), entry))
            }
            val withdraw = BarDataSet(withdrawCategory, resources.getString(R.string.withdrawal))
            val deposit = BarDataSet(depositCategory, resources.getString(R.string.deposit))
            withdraw.apply {
                valueFormatter = LargeValueFormatter()
                valueTextColor = Color.RED
                color = Color.RED
                valueTextSize = 12f
            }
            deposit.apply {
                valueFormatter = LargeValueFormatter()
                valueTextColor = Color.GREEN
                color = Color.GREEN
                valueTextSize = 12f
            }
            binding.categoryChart.apply {
                description.isEnabled = false
                /*
                 * 20 Nov 2020,  MPAndroidChart(v3.1.0)
                 * This is a hack which took me a day to conceive.
                 * In order for X Axis to correctly show the labels, there should be an empty element between each element.
                 * I have tried changing the width using barData#getGroupWidth() and groupBars(), the bar is either
                 * outside of the chart(last bar) or has a huge margin at the start(first bar)
                 * Sometimes, IndexAxisValueFormatter is crammed to only half of the chart and with a huge
                 * margin at the end if I use barData#barWidth
                 *
                 * Conclusion: The valueFormatter works for now....
                 */
                xAxis.valueFormatter = IndexAxisValueFormatter(
                        listOf(DateTimeUtil.getDayAndMonth(DateTimeUtil.getStartOfMonth()),
                                "",
                                DateTimeUtil.getDayAndMonth(DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 1)),
                                "",
                                DateTimeUtil.getDayAndMonth(DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 2)),
                                "",
                                DateTimeUtil.getDayAndMonth(DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 3)),
                                "",
                                DateTimeUtil.getDayAndMonth(DateTimeUtil.getEndOfMonth())))
                data = BarData(deposit, withdraw)
                groupBars(0f, 0.4f, 0f)
                xAxis.axisMaximum = barData.getGroupWidth(0.4f, 0f) * 5
                xAxis.setCenterAxisLabels(true)
                data.isHighlightEnabled = false
                animateY(1000)
            }
        }
        setColor()
    }


    private fun loadTransactionList(){
        val transactionAdapter = TransactionSeparatorAdapter{ data -> itemClicked(data) }
        binding.transactionList.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.transactionList.adapter = transactionAdapter
        categoryDetailViewModel.getTransactionList().observe(viewLifecycleOwner){ transactionList ->
            transactionAdapter.submitData(lifecycle, transactionList)
        }
    }

    private fun itemClicked(data: Transactions){
        parentFragmentManager.commit {
            replace(R.id.fragment_container, TransactionDetailsFragment().apply {
                arguments = bundleOf("transactionJournalId" to data.transaction_journal_id)
            })
            addToBackStack(null)
        }
    }

    private fun setColor(){
        if(globalViewModel.isDark){
            binding.categoryChart.legend.textColor = getCompatColor(R.color.md_white_1000)
            binding.categoryChart.axisLeft.textColor = getCompatColor(R.color.md_white_1000)
            binding.categoryChart.axisRight.textColor = getCompatColor(R.color.md_white_1000)
            binding.categoryChart.xAxis.textColor = getCompatColor(R.color.md_white_1000)
        }

    }

    override fun deleteItem() {
        categoryDetailViewModel.deleteCategory().observe(viewLifecycleOwner){ isDeleted ->
            if(isDeleted){
                toastSuccess("$categoryName deleted")
                parentFragmentManager.popBackStack()
            } else {
                toastOffline("$categoryName will be deleted later")
            }
        }
    }

    override fun editItem() {
        val addCategoryFragment = AddCategoriesFragment().apply {
            arguments = bundleOf("categoryId" to categoryId)
        }
        addCategoryFragment.show(parentFragmentManager, "add_category_fragment")
    }
}