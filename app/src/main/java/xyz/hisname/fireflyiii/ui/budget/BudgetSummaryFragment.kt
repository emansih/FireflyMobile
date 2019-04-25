package xyz.hisname.fireflyiii.ui.budget

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
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
import xyz.hisname.fireflyiii.util.extension.toastInfo
import xyz.hisname.fireflyiii.util.extension.zipLiveData
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class BudgetSummaryFragment: BaseFragment() {

    private val budgetLimit by lazy { getViewModel(BudgetViewModel::class.java) }
    private val coloring = arrayListOf<Int>()
    private var budgetSpent = 0.toFloat()
    private var budgeted = 0.toFloat()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.create(R.layout.fragment_budget_summary, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        for (col in ColorTemplate.MATERIAL_COLORS) {
            coloring.add(col)
        }
        budgetSummaryPieChart.setNoDataText(resources.getString(R.string.no_data_to_generate_chart))
        currencyViewModel.getDefaultCurrency().observe(this, Observer { currency ->
            setText(currency[0])
            getExpensesTransaction(currency[0])
        })
    }

    private fun getExpensesTransaction(currencyData: CurrencyData) {
        val currencyCode = currencyData.currencyAttributes?.code ?: ""
        zipLiveData(transactionViewModel.getTotalTransactionAmountByDateAndCurrency(DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), currencyCode, "Withdrawal"),
                transactionViewModel.getUniqueBudgetByDate(DateTimeUtil.getStartOfMonth(),
                        DateTimeUtil.getEndOfMonth(), currencyCode, "Withdrawal")).observe(this, Observer { transactionData ->
            if(transactionData.second.isNotEmpty()) {
                val pieEntryArray: ArrayList<PieEntry> = ArrayList(transactionData.second.size)
                transactionData.second.forEachIndexed { _, uniqueBudget ->
                    transactionViewModel.getTransactionByDateAndBudgetAndCurrency(DateTimeUtil.getStartOfMonth(),
                            DateTimeUtil.getEndOfMonth(), currencyCode,
                            "Withdrawal", uniqueBudget).observe(this, Observer { transactionAmount ->
                        val percentageCategory: Double = transactionAmount.absoluteValue.roundToInt().toDouble().div(transactionData.first.absoluteValue.roundToInt().toDouble()).times(100)
                        if (uniqueBudget == "null" || uniqueBudget == null) {
                            pieEntryArray.add(PieEntry(percentageCategory.roundToInt().toFloat(), "No Category", transactionAmount))
                        } else {
                            pieEntryArray.add(PieEntry(percentageCategory.roundToInt().toFloat(), uniqueBudget, transactionAmount))
                        }
                        val pieDataSet = PieDataSet(pieEntryArray, "")
                        pieDataSet.valueFormatter = MpAndroidPercentFormatter()
                        pieDataSet.colors = coloring
                        pieDataSet.valueTextSize = 15f
                        budgetSummaryPieChart.data = PieData(pieDataSet)
                        budgetSummaryPieChart.invalidate()
                    })
                }
            }
        })
        budgetSummaryPieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {
                budgetAmountValue.text = currencyData.currencyAttributes?.symbol + " " + budgeted
                actualAmountValue.text = currencyData.currencyAttributes?.symbol + " " + budgetSpent
                remainingAmountValue.text = currencyData.currencyAttributes?.symbol + " " +
                        LocaleNumberParser.parseDecimal((budgeted - budgetSpent).toDouble(), requireContext())
            }

            override fun onValueSelected(entry: Entry, high: Highlight) {
                val pe = entry as PieEntry
                val transactionDialog  = TransactionByBudgetDialogFragment()
                transactionDialog.arguments = bundleOf("budgetName" to pe.label)
                transactionDialog.show(requireFragmentManager(), "transaction_budget_dialog")

            }

        })
    }

    private fun setText(currencyData: CurrencyData){
        budgetDuration.text = DateTimeUtil.getDurationText()
        zipLiveData(budgetLimit.retrieveSpentBudget(),
                budgetLimit.retrieveCurrentMonthBudget(currencyData.currencyAttributes?.code ?: "")).observe(this, Observer { budget ->
            budgetSpent = budget.first.toFloat()
            budgeted = budget.second.toFloat()
            budgetAmountValue.text = currencyData.currencyAttributes?.symbol + " " + budgeted
            actualAmountValue.text = currencyData.currencyAttributes?.symbol + " " + budgetSpent
            remainingAmountValue.text = currencyData.currencyAttributes?.symbol + " " +
                    LocaleNumberParser.parseDecimal((budgeted - budgetSpent).toDouble(), requireContext())
        })
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