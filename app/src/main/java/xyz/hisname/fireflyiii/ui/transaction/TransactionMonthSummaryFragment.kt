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

package xyz.hisname.fireflyiii.ui.transaction

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.FragmentTransactionMonthSummaryBinding
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.getImprovedViewModel
import xyz.hisname.fireflyiii.util.extension.setData
import xyz.hisname.fireflyiii.util.extension.toastInfo

class TransactionMonthSummaryFragment: BaseFragment() {

    private val coloring = arrayListOf<Int>()
    private val transactionType  by lazy { arguments?.getString("transactionType") ?: "" }
    private val monthYear by lazy { arguments?.getInt("monthYear") ?: 0}
    private val transactionMonthViewModel by lazy { getImprovedViewModel(TransactionMonthViewModel::class.java) }
    private val transactionAdapter by lazy { TransactionSeparatorAdapter{ data -> itemClicked(data) } }
    private var fragmentTransactionMonthSummaryBinding: FragmentTransactionMonthSummaryBinding? = null
    private val binding get() = fragmentTransactionMonthSummaryBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentTransactionMonthSummaryBinding = FragmentTransactionMonthSummaryBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        for (col in ColorTemplate.COLORFUL_COLORS) {
            coloring.add(col)
        }
        for (col in ColorTemplate.JOYFUL_COLORS){
            coloring.add(col)
        }
        binding.monthlySummaryText.text = getString(R.string.chart_transaction_in_period, transactionType,
                getStartOfMonth(monthYear), getEndOfMonth(monthYear))
        setCategoryChart()
        setRecyclerView()
    }

    private fun getStartOfMonth(monthYear: Int): String{
        return if(monthYear == 0){
            DateTimeUtil.getStartOfMonth()
        } else {
            DateTimeUtil.getStartOfMonth(monthYear.toLong())
        }
    }

    private fun getEndOfMonth(monthYear: Int): String{
        return if(monthYear == 0){
            DateTimeUtil.getEndOfMonth()
        } else {
            DateTimeUtil.getEndOfMonth(monthYear.toLong())
        }
    }

    private fun setCategoryChart(){
        transactionMonthViewModel.getCategoryData(transactionType, monthYear).observe(viewLifecycleOwner){ categorySumList ->
            val pieEntryArray = arrayListOf<PieEntry>()
            categorySumList.forEach { categorySum ->
                pieEntryArray.add(PieEntry(categorySum.first, categorySum.second, categorySum.third))
            }
            val pieDataSet = PieDataSet(pieEntryArray, "").apply {
                colors = coloring
                valueTextSize = 15f
                valueFormatter = PercentFormatter(binding.categoryPieChart)
            }
            binding.categoryPieChart.description.isEnabled = false
            binding.categoryPieChart.invalidate()
            binding.categoryPieChart.data = PieData(pieDataSet)
            binding.categoryPieChart.setData {
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(entry: Entry?, h: Highlight?) {
                        val pe = entry as PieEntry
                        val entryLabel = if(entry.label.isBlank()){
                            getString(R.string.expenses_without_category)
                        } else {
                            entry.label
                        }
                         toastInfo(entryLabel + ": " + transactionMonthViewModel.currencySymbol + entry.data)
                    }

                    override fun onNothingSelected() {}

                })
            }
            setBudgetChart()
            getAccounts()
        }

        transactionMonthViewModel.totalSumLiveData.observe(viewLifecycleOwner){ total ->
            binding.monthlyTotalText.text = "Total: " +  transactionMonthViewModel.currencySymbol + total
        }

    }

    private fun setBudgetChart(){
        transactionMonthViewModel.getBudgetData(transactionType, monthYear).observe(viewLifecycleOwner){ budgetSumList ->
            val pieEntryArray = arrayListOf<PieEntry>()
            budgetSumList.forEach { budgetSum ->
                pieEntryArray.add(PieEntry(budgetSum.first, budgetSum.second, budgetSum.third))
            }
            val pieDataSet = PieDataSet(pieEntryArray, "").apply {
                colors = coloring
                valueTextSize = 15f
                valueFormatter = PercentFormatter(binding.budgetPieChart)
            }
            binding.budgetPieChart.description.isEnabled = false
            binding.budgetPieChart.invalidate()
            binding.budgetPieChart.data = PieData(pieDataSet)
            binding.budgetPieChart.setData {
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(entry: Entry?, h: Highlight?) {
                        val pe = entry as PieEntry
                        val entryLabel = if(entry.label.isBlank()){
                            getString(R.string.expenses_without_category)
                        } else {
                            entry.label
                        }
                        toastInfo(entryLabel + ": " + transactionMonthViewModel.currencySymbol + entry.data)
                    }
                    override fun onNothingSelected() {}
                })
            }
        }
    }

    private fun getAccounts(){
        transactionMonthViewModel.getAccount(transactionType, monthYear).observe(viewLifecycleOwner){ accountSumList ->
            val pieEntryArray = arrayListOf<PieEntry>()
            accountSumList.forEach { accountSum ->
                pieEntryArray.add(PieEntry(accountSum.first, accountSum.second, accountSum.third))
            }
            val pieDataSet = PieDataSet(pieEntryArray, "").apply {
                colors = coloring
                valueTextSize = 15f
                valueFormatter = PercentFormatter(binding.accountPieChart)
            }
            binding.accountPieChart.description.isEnabled = false
            binding.accountPieChart.invalidate()
            binding.accountPieChart.data = PieData(pieDataSet)
            binding.accountPieChart.setData {
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(entry: Entry?, h: Highlight?) {
                        val pe = entry as PieEntry
                        val entryLabel = if(entry.label.isBlank()){
                            getString(R.string.expenses_without_category)
                        } else {
                            entry.label
                        }
                        toastInfo(entryLabel + ": " + transactionMonthViewModel.currencySymbol + entry.data)
                    }
                    override fun onNothingSelected() {}
                })
            }
        }
    }

    private fun setRecyclerView(){
        binding.transactionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.transactionRecyclerView.adapter = transactionAdapter
        transactionMonthViewModel.getTransactionList(transactionType, monthYear).observe(viewLifecycleOwner){ pagingData ->
            transactionAdapter.submitData(lifecycle, pagingData)
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val mainToolbar = requireActivity().findViewById<Toolbar>(R.id.activity_toolbar)
        mainToolbar.title = "$transactionType Summary"
    }

}