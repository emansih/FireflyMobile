package xyz.hisname.fireflyiii.ui.budget

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_budget_summary.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.budget.BudgetViewModel
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.transaction.TransactionByBudgetDialogFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.LocaleNumberParser
import xyz.hisname.fireflyiii.util.MpAndroidPercentFormatter
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.setData
import xyz.hisname.fireflyiii.util.extension.zipLiveData
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class BudgetSummaryFragment: BaseFragment() {

    private val budgetLimit by lazy { getViewModel(BudgetViewModel::class.java) }
    private val coloring = arrayListOf<Int>()
    private var budgetSpent = 0f
    private var budgeted = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.create(R.layout.fragment_budget_summary, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        for (col in ColorTemplate.COLORFUL_COLORS) {
            coloring.add(col)
        }
        for (col in ColorTemplate.JOYFUL_COLORS){
            coloring.add(col)
        }
        budgetSummaryPieChart.isDrawHoleEnabled = false
        currencyViewModel.getDefaultCurrency().observe(this) { currency ->
            retrieveData(currency[0])
            setBudgetSummary(currency[0])
        }
    }

    private fun retrieveData(currencyData: CurrencyData){
        val currencyCode = currencyData.currencyAttributes?.code ?: ""
        zipLiveData(zipLiveData(transactionViewModel.getTotalTransactionAmountByDateAndCurrency(DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), currencyCode, "Withdrawal"),
                transactionViewModel.getUniqueBudgetByDate(DateTimeUtil.getStartOfMonth(),
                        DateTimeUtil.getEndOfMonth(), currencyCode, "Withdrawal")),
                zipLiveData(budgetLimit.retrieveSpentBudget(),
                        budgetLimit.retrieveCurrentMonthBudget(currencyData.currencyAttributes?.code ?: ""))).observe(this) { fireflyData ->
            if(fireflyData.first.second.isNotEmpty()) {
                val pieEntryArray: ArrayList<PieEntry> = ArrayList(fireflyData.first.second.size)
                fireflyData.first.second.forEachIndexed { _, uniqueBudget ->
                    transactionViewModel.getTransactionByDateAndBudgetAndCurrency(DateTimeUtil.getStartOfMonth(),
                            DateTimeUtil.getEndOfMonth(), currencyCode,
                            "Withdrawal", uniqueBudget).observe(this) { transactionAmount ->
                        val percentageCategory = transactionAmount.absoluteValue.roundToInt()
                                .toDouble()
                                .div(fireflyData.first.first.absoluteValue.roundToInt().toDouble())
                                .times(100)
                        if (uniqueBudget == "null" || uniqueBudget == null) {
                            pieEntryArray.add(PieEntry(percentageCategory.roundToInt().toFloat(),
                                    requireContext().getString(R.string.expenses_without_budget),
                                    transactionAmount))
                        } else {
                            pieEntryArray.add(PieEntry(percentageCategory.roundToInt().toFloat(), uniqueBudget, transactionAmount))
                        }
                        val pieDataSet = PieDataSet(pieEntryArray, "")
                        pieDataSet.valueFormatter = MpAndroidPercentFormatter()
                        pieDataSet.colors = coloring
                        pieDataSet.valueTextSize = 15f
                        budgetSummaryPieChart.data = PieData(pieDataSet)
                        budgetSummaryPieChart.invalidate()
                    }
                }
            }
            setBudgetData(fireflyData.second, currencyData)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setBudgetData(fireflyData: Pair<String, String>, currencyData: CurrencyData){
        val currencySymbol = currencyData.currencyAttributes?.symbol ?: ""
        budgetSpent = fireflyData.first.toFloat()
        budgeted = fireflyData.second.toFloat()
        budgetAmountValue.text = "$currencySymbol $budgeted"
        actualAmountValue.text = "$currencySymbol $budgetSpent"
        remainingAmountValue.text = currencySymbol + " " +
                LocaleNumberParser.parseDecimal((budgeted - budgetSpent).toDouble(), requireContext())

    }

    @SuppressLint("SetTextI18n")
    private fun setBudgetSummary(currencyData: CurrencyData){
        val currencyCode = currencyData.currencyAttributes?.code ?: ""
        budgetSummaryPieChart.setData {
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                    budgetAmountValue.text = currencyData.currencyAttributes?.symbol + " " + budgeted
                    actualAmountValue.text = currencyData.currencyAttributes?.symbol + " " + budgetSpent
                    remainingAmountValue.text = currencyData.currencyAttributes?.symbol + " " +
                            LocaleNumberParser.parseDecimal((budgeted - budgetSpent).toDouble(), requireContext())
                    showTransactionButton.isGone = true
                }

                override fun onValueSelected(entry: Entry, high: Highlight) {
                    val pe = entry as PieEntry
                    showTransactionButton.isVisible = true
                    budgetAmountValue.text = "--.--"
                    actualAmountValue.text = "--.--"
                    remainingAmountValue.text = "--.--"
                    transactionViewModel.getTransactionByDateAndBudgetAndCurrency(DateTimeUtil.getStartOfMonth(),
                            DateTimeUtil.getEndOfMonth(), currencyCode, "Withdrawal",
                            pe.label).observe(this@BudgetSummaryFragment) { value ->
                        budgetAmountValue.text = currencyData.currencyAttributes?.symbol + " " + entry.value
                        actualAmountValue.text = currencyData.currencyAttributes?.symbol + " " +
                                LocaleNumberParser.parseDecimal(value, requireContext())
                        remainingAmountValue.text = currencyData.currencyAttributes?.symbol + " " +
                                LocaleNumberParser.parseDecimal((entry.value - Math.abs(value)), requireContext())

                    }
                    showTransactionButton.setOnClickListener {
                        val transactionDialog = TransactionByBudgetDialogFragment()
                        transactionDialog.arguments = bundleOf("budgetName" to pe.label)
                        transactionDialog.show(requireFragmentManager(), "transaction_budget_dialog")
                    }
                }
            })
        }
        budgetSummaryPieChart.description.isEnabled = false
        budgetDuration.text = DateTimeUtil.getDurationText()
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        fab.isGone = true
        activity?.activity_toolbar?.title = "Budget Summary"
    }

    override fun onResume() {
        super.onResume()
        fab.isGone = true
        activity?.activity_toolbar?.title = "Budget Summary"
    }

    override fun handleBack() {
        requireFragmentManager().popBackStack()
    }


}