package xyz.hisname.fireflyiii.ui.account

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.fragment_account_detail.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.ui.transaction.TransactionAdapter
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*

class AccountDetailFragment: BaseDetailFragment() {

    private val accountId: Long by lazy { arguments?.getLong("accountId") as Long  }
    private val accountType  by lazy { arguments?.getString("accountType")  }
    private val transactionAdapter by lazy { TransactionAdapter{ data -> itemClicked(data) } }
    private val accountDetailViewModel by lazy { getImprovedViewModel(AccountDetailViewModel::class.java) }

    private val coloring = arrayListOf<Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_account_detail, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        for (col in ColorTemplate.COLORFUL_COLORS) {
            coloring.add(col)
        }
        for (col in ColorTemplate.JOYFUL_COLORS){
            coloring.add(col)
        }
        accountDetailViewModel.getAccountById(accountId).observe(viewLifecycleOwner){ accountData ->
            setLineChart()
            setExpensesByCategory()
            setExpensesByBudget()
            setIncomeByCategory()
            getAccountTransaction()
        }
        setDarkMode()
    }

    private fun setLineChart(){
        accountDetailViewModel.lineChartData.observe(viewLifecycleOwner){ amount ->
            val lineChartEntries = arrayListOf(
                    Entry(0f, amount.first.toFloat()),
                    Entry(1f, amount.second.toFloat()),
                    Entry(2f, amount.third.toFloat()),
                    Entry(3f, amount.fourth.toFloat()),
                    Entry(4f, amount.fifth.toFloat()),
                    Entry(5f, amount.sixth.toFloat())
            )
            val dataSet = LineDataSet(lineChartEntries, accountDetailViewModel.accountName)
            dataSet.apply {
                setCircleColor(getCompatColor(R.color.colorAccent))
                valueTextColor = Color.GREEN
                valueTextSize = 15f
            }
            val lineChartData = LineData(dataSet)
            transactionLineChart.apply {
                xAxis.granularity = 1f
                xAxis.valueFormatter = IndexAxisValueFormatter(arrayListOf(
                        DateTimeUtil.getStartOfMonth(),
                        DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 1),
                        DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 2),
                        DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 3),
                        DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 4),
                        DateTimeUtil.getEndOfMonth()))
                xAxis.setDrawLabels(true)
                xAxis.setDrawAxisLine(false)
                xAxis.setDrawGridLines(false)
                data = lineChartData
                axisRight.isEnabled = false
                description.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                data.isHighlightEnabled = false
                setTouchEnabled(true)
                setPinchZoom(true)
                animateY(1000, Easing.EaseOutBack)
            }
        }
        balanceHistoryCardText.text = resources.getString(R.string.account_chart_description,
                accountDetailViewModel.accountName, DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth())
    }

    private fun setExpensesByCategory(){
        accountDetailViewModel.uniqueExpensesCategoryLiveData.observe(viewLifecycleOwner){ categorySumList ->
            if(categorySumList.isEmpty()){
                categoryPieChart.setNoDataText(resources.getString(R.string.no_data_to_generate_chart))
            } else {
                val pieEntryArray = arrayListOf<PieEntry>()
                categorySumList.forEach { categorySum ->
                pieEntryArray.add(PieEntry(categorySum.first, categorySum.second, categorySum.third))
                }
                val pieDataSet = PieDataSet(pieEntryArray, "").apply {
                    colors = coloring
                    valueTextSize = 15f
                    valueFormatter = PercentFormatter(categoryPieChart)
                }
                categoryPieChart.description.isEnabled = false
                categoryPieChart.invalidate()
                categoryPieChart.data = PieData(pieDataSet)
                categoryPieChart.setData {
                    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                        override fun onValueSelected(entry: Entry?, h: Highlight?) {
                            val pe = entry as PieEntry
                            val entryLabel = if(entry.label.isBlank()){
                                getString(R.string.expenses_without_category)
                            } else {
                                entry.label
                            }
                            toastInfo(entryLabel + ": " + accountDetailViewModel.currencySymbol + entry.data)
                        }

                        override fun onNothingSelected() {}

                    })
                }
            }
        }
    }

    private fun setExpensesByBudget(){
        accountDetailViewModel.uniqueBudgetLiveData.observe(viewLifecycleOwner){ budgetSumList ->
            if(budgetSumList.isEmpty()){
                budgetPieChart.setNoDataText(resources.getString(R.string.no_data_to_generate_chart))
            } else {
                val pieEntryArray = arrayListOf<PieEntry>()
                budgetSumList.forEach { budgetSum ->
                    pieEntryArray.add(PieEntry(budgetSum.first, budgetSum.second, budgetSum.third))
                }
                val pieDataSet = PieDataSet(pieEntryArray, "").apply {
                    colors = coloring
                    valueTextSize = 15f
                    valueFormatter = PercentFormatter(budgetPieChart)
                }
                budgetPieChart.description.isEnabled = false
                budgetPieChart.invalidate()
                budgetPieChart.data = PieData(pieDataSet)
                budgetPieChart.setData {
                    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                        override fun onValueSelected(entry: Entry?, h: Highlight?) {
                            val pe = entry as PieEntry
                            val entryLabel = if (entry.label.isBlank()) {
                                getString(R.string.expenses_without_budget)
                            } else {
                                entry.label
                            }
                            toastInfo(entryLabel + ": " + accountDetailViewModel.currencySymbol + entry.data)
                        }

                        override fun onNothingSelected() {}
                    })
                }
            }
        }
    }

    private fun setDarkMode(){
        if(isDarkMode()){
            transactionLineChart.xAxis.textColor = getCompatColor(R.color.md_white_1000)
            transactionLineChart.legend.textColor = getCompatColor(R.color.md_white_1000)
            transactionLineChart.axisLeft.textColor = getCompatColor(R.color.md_white_1000)
            categoryPieChart.legend.textColor = getCompatColor(R.color.md_white_1000)
            budgetPieChart.legend.textColor = getCompatColor(R.color.md_white_1000)
        }
    }

    private fun setIncomeByCategory(){
        accountDetailViewModel.uniqueIncomeCategoryLiveData.observe(viewLifecycleOwner) { categorySumList ->
            if (categorySumList.isEmpty()) {
                incomePieChart.setNoDataText(resources.getString(R.string.no_data_to_generate_chart))
            } else {
                val pieEntryArray = arrayListOf<PieEntry>()
                categorySumList.forEach { categorySum ->
                    pieEntryArray.add(PieEntry(categorySum.first, categorySum.second, categorySum.third))
                }
                val pieDataSet = PieDataSet(pieEntryArray, "").apply {
                    colors = coloring
                    valueTextSize = 15f
                    valueFormatter = PercentFormatter(incomePieChart)
                }
                incomePieChart.description.isEnabled = false
                incomePieChart.invalidate()
                incomePieChart.data = PieData(pieDataSet)
                incomePieChart.setData {
                    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                        override fun onValueSelected(entry: Entry?, h: Highlight?) {
                            val pe = entry as PieEntry
                            val entryLabel = if (entry.label.isBlank()) {
                                getString(R.string.income_without_category)
                            } else {
                                entry.label
                            }
                            toastInfo(entryLabel + ": " + accountDetailViewModel.currencySymbol + entry.data)
                        }

                        override fun onNothingSelected() {}

                    })
                }
            }
        }
    }

    private fun getAccountTransaction(){
        accountTransactionList.layoutManager = LinearLayoutManager(requireContext())
        accountTransactionList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        accountTransactionList.adapter = transactionAdapter
        accountDetailViewModel.getTransactionList(accountId).observe(viewLifecycleOwner){ list ->
            transactionAdapter.submitData(lifecycle, list)
        }
    }

    private fun itemClicked(data: Transactions){
        parentFragmentManager.commit {
            replace(R.id.fragment_container, TransactionDetailsFragment().apply {
                arguments = bundleOf("transactionId" to data.transaction_journal_id)
            })
            addToBackStack(null)
        }
    }

    override fun deleteItem() {
        accountViewModel.isLoading.observe(viewLifecycleOwner){
            if(it == true){
                ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            }
        }
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_account_title, accountDetailViewModel.accountName))
                .setMessage(resources.getString(R.string.delete_account_message, accountDetailViewModel.accountName))
                .setPositiveButton(R.string.delete_permanently) { _, _ ->
                    accountViewModel.deleteAccountById(accountId).observe(viewLifecycleOwner) { isAccountDeleted ->
                        if(isAccountDeleted){
                            parentFragmentManager.popBackStack()
                            when (accountType) {
                                "asset" -> {
                                    toastSuccess(resources.getString(R.string.asset_account_deleted, accountDetailViewModel.accountName))
                                }
                                "expense" -> {
                                    toastSuccess(resources.getString(R.string.expense_account_deleted, accountDetailViewModel.accountName))
                                }
                                "revenue" -> {
                                    toastSuccess(resources.getString(R.string.revenue_account_deleted, accountDetailViewModel.accountName))
                                }
                                else -> {
                                    toastSuccess("Account Deleted")
                                }
                            }
                        }
                    }
                }
                .setNegativeButton(android.R.string.no){dialog, _ ->
                    dialog.dismiss()
                }
                .show()
    }

    override fun editItem() {
        parentFragmentManager.commit {
            replace(R.id.bigger_fragment_container, AddAccountFragment().apply{
                arguments = bundleOf("accountType" to accountType, "accountId" to accountId)
            })
            addToBackStack(null)
        }
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }
}