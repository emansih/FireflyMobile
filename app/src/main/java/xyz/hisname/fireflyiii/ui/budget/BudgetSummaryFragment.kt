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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isGone
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.FragmentBudgetSummaryBinding
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.ui.transaction.TransactionSeparatorAdapter
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.math.BigDecimal
import java.util.*

class BudgetSummaryFragment: BaseFragment(), AdapterView.OnItemSelectedListener {

    private val transactionExtendedFab by bindView<ExtendedFloatingActionButton>(R.id.addTransactionExtended)
    private val budgetSummaryViewModel by lazy { getImprovedViewModel(BudgetSummaryViewModel::class.java) }
    private val transactionAdapter by lazy { TransactionSeparatorAdapter{ data -> itemClicked(data) } }
    private val coloring = arrayListOf<Int>()
    private var fragmentBudgetSummary: FragmentBudgetSummaryBinding? = null
    private val binding get() = fragmentBudgetSummary!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentBudgetSummary = FragmentBudgetSummaryBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSpinner()
        setText()
        setWidget()
        loadTransactionList(null)
        budgetSummaryViewModel.pieChartData.observe(viewLifecycleOwner){ list ->
            setPieChart(list)
        }
    }

    private fun setWidget(){
        transactionExtendedFab.isGone = true
        for (col in ColorTemplate.COLORFUL_COLORS) {
            coloring.add(col)
        }
        for (col in ColorTemplate.JOYFUL_COLORS){
            coloring.add(col)
        }
        binding.monthAndYearText.text = DateTimeUtil.getMonthAndYear(DateTimeUtil.getTodayDate())
        binding.transactionList.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.transactionList.adapter = transactionAdapter
        binding.budgetSummaryPieChart.isDrawHoleEnabled = false
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
            budgetSummaryViewModel.monthCount -=1
            setDate()
            transactionAdapter.notifyDataSetChanged()
            binding.budgetSummaryPieChart.clear()
        }
        binding.nextMonthArrow.setOnClickListener {
            budgetSummaryViewModel.monthCount +=1
            setDate()
            transactionAdapter.notifyDataSetChanged()
            binding.budgetSummaryPieChart.clear()
        }
        setDate()
    }

    private fun setDate(){
        budgetSummaryViewModel.setDisplayDate().observe(viewLifecycleOwner){ dateToDisplay ->
            binding.monthAndYearText.text = dateToDisplay
        }
    }

    private fun setSpinner(){
        binding.currencySpinner.onItemSelectedListener = this
        budgetSummaryViewModel.getCurrency().observe(viewLifecycleOwner){ currencyDataList ->
            val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, currencyDataList)
            binding.currencySpinner.adapter = arrayAdapter
        }
    }

    private fun setText(){
        budgetSummaryViewModel.availableBudget.observe(viewLifecycleOwner){ available ->
            binding.budgetAmountValue.text = available
        }
        budgetSummaryViewModel.totalTransaction.observe(viewLifecycleOwner){ total ->
            binding.actualAmountValue.text = total
        }
        budgetSummaryViewModel.balanceBudget.observe(viewLifecycleOwner){ balance ->
            binding.remainingAmountValue.text = balance.toString()
        }
    }

    private fun setPieChart(pieChartData: List<Triple<Float, String, BigDecimal>>){
        val pieEntryArray: ArrayList<PieEntry> = arrayListOf()
        pieChartData.forEach { data ->
            pieEntryArray.add(PieEntry(data.first, data.second, data.third))
        }

        val pieDataSet = PieDataSet(pieEntryArray, "")

        pieDataSet.valueFormatter = PercentFormatter(binding.budgetSummaryPieChart)
        pieDataSet.colors = coloring
        pieDataSet.valueTextSize = 15f
        binding.budgetSummaryPieChart.description.isEnabled = false
        binding.budgetSummaryPieChart.invalidate()
        binding.budgetSummaryPieChart.data = PieData(pieDataSet)

        binding.budgetSummaryPieChart.setData {
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(entry: Entry, h: Highlight) {
                    val pe = entry as PieEntry
                    val entryLabel = if(entry.label == requireContext().getString(R.string.expenses_without_budget)){
                        ""
                    } else {
                        entry.label
                    }
                    budgetSummaryViewModel.getBalance(entryLabel)
                    loadTransactionList(entryLabel)
                }

                override fun onNothingSelected() {
                    loadTransactionList(null)
                    budgetSummaryViewModel.availableBudget.postValue(budgetSummaryViewModel.originalBudgetString)
                    budgetSummaryViewModel.totalTransaction.postValue(budgetSummaryViewModel.originalSpentString)
                    budgetSummaryViewModel.balanceBudget.postValue(budgetSummaryViewModel.originalRemainderString)
                }

            })
        }
        loadTransactionList(null)
    }

    private fun itemClicked(data: Transactions){
        parentFragmentManager.commit {
            replace(R.id.fragment_container, TransactionDetailsFragment().apply {
                arguments = bundleOf("transactionJournalId" to data.transaction_journal_id)
            })
            addToBackStack(null)
        }
    }

    private fun loadTransactionList(budget: String?){
       budgetSummaryViewModel.getTransactionList(budget).observe(viewLifecycleOwner){ transactionList ->
           transactionAdapter.submitData(lifecycle,transactionList)
       }
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = resources.getString(R.string.budget)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = resources.getString(R.string.budget)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        budgetSummaryViewModel.changeCurrency(position)
        transactionAdapter.notifyDataSetChanged()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentBudgetSummary = null
    }

}