package xyz.hisname.fireflyiii.ui.budget

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_budget_summary.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.transaction.TransactionRecyclerAdapter
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.math.BigDecimal
import java.util.*

class BudgetSummaryFragment: BaseFragment(), AdapterView.OnItemSelectedListener {

    private val transactionExtendedFab by bindView<ExtendedFloatingActionButton>(R.id.addTransactionExtended)
    private val budgetSummaryViewModel by lazy { getImprovedViewModel(BudgetSummaryViewModel::class.java) }
    private var dataAdapter = arrayListOf<Transactions>()
    private val rtAdapter by lazy { TransactionRecyclerAdapter(dataAdapter){ data -> itemClicked(data) } }
    private val coloring = arrayListOf<Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.create(R.layout.fragment_budget_summary, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionExtendedFab.isGone = true
        for (col in ColorTemplate.COLORFUL_COLORS) {
            coloring.add(col)
        }
        for (col in ColorTemplate.JOYFUL_COLORS){
            coloring.add(col)
        }
        budgetSummaryPieChart.isDrawHoleEnabled = false
        setSpinner()
        setText()
        budgetSummaryViewModel.pieChartData.observe(viewLifecycleOwner){ list ->
            setPieChart(list)
        }
        transactionList.layoutManager = LinearLayoutManager(requireContext())
        transactionList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        transactionList.adapter = rtAdapter
        loadTransactionList(null)
    }

    private fun setSpinner(){
        currencySpinner.onItemSelectedListener = this
        budgetSummaryViewModel.getCurrency().observe(viewLifecycleOwner){ currencyDataList ->
            val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, currencyDataList)
            currencySpinner.adapter = arrayAdapter
        }
    }

    private fun setText(){
        budgetDuration.text = DateTimeUtil.getDurationText()
        budgetSummaryViewModel.availableBudget.observe(viewLifecycleOwner){ available ->
            budgetAmountValue.text = available
        }
        budgetSummaryViewModel.totalTransaction.observe(viewLifecycleOwner){ total ->
            actualAmountValue.text = total
        }
        budgetSummaryViewModel.balanceBudget.observe(viewLifecycleOwner){ balance ->
            remainingAmountValue.text = balance.toString()
        }
    }

    private fun setPieChart(pieChartData: List<Triple<Float, String, BigDecimal>>){
        val pieEntryArray: ArrayList<PieEntry> = arrayListOf()
        pieChartData.forEach { data ->
            pieEntryArray.add(PieEntry(data.first, data.second, data.third))
        }

        val pieDataSet = PieDataSet(pieEntryArray, "")

        pieDataSet.valueFormatter = PercentFormatter(budgetSummaryPieChart)
        pieDataSet.colors = coloring
        pieDataSet.valueTextSize = 15f
        budgetSummaryPieChart.description.isEnabled = false
        budgetSummaryPieChart.invalidate()
        budgetSummaryPieChart.data = PieData(pieDataSet)

        budgetSummaryPieChart.setData {
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
           dataAdapter.clear()
           dataAdapter.addAll(transactionList)
           rtAdapter.update(transactionList)
           rtAdapter.notifyDataSetChanged()
       }
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        extendedFab.isGone = true
        activity?.activity_toolbar?.title = getString(R.string.budget)
    }

    override fun onResume() {
        super.onResume()
        extendedFab.isGone = true
        activity?.activity_toolbar?.title = getString(R.string.budget)
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        budgetSummaryViewModel.changeCurrency(position)
        dataAdapter.clear()
        rtAdapter.notifyDataSetChanged()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }


}