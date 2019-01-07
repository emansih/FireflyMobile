package xyz.hisname.fireflyiii.ui.dashboard

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.budget.BudgetViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyViewModel
import xyz.hisname.fireflyiii.repository.transaction.TransactionsViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.zipLiveData
import kotlin.math.roundToInt

class DashboardFragment: BaseFragment() {

    private val transactionViewModel by lazy { getViewModel(TransactionsViewModel::class.java) }
    private val currencyViewModel by lazy { getViewModel(CurrencyViewModel::class.java) }
    private val budgetLimit by lazy { getViewModel(BudgetViewModel::class.java) }
    private var depositSum = 0
    private var withdrawSum = 0
    private var transaction = 0
    private var budgetSpent = 0f
    private var budgeted = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_dashboard,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMonthlySummaryCard()
        setBarChart()
    }

    @SuppressLint("SetTextI18n")
    private fun setMonthlySummaryCard(){
        currencyViewModel.getDefaultCurrency().observe(this, Observer { defaultCurrency ->
            if(defaultCurrency.isNotEmpty()) {
                val currencyData = defaultCurrency[0].currencyAttributes
                val currencyCode = currencyData?.code!!
                zipLiveData(transactionViewModel.getWithdrawalWithCurrencyCode(DateTimeUtil.getStartOfMonth(),
                        DateTimeUtil.getEndOfMonth(), currencyCode),
                        transactionViewModel.getDepositWithCurrencyCode(DateTimeUtil.getStartOfMonth(),
                                DateTimeUtil.getEndOfMonth(), currencyCode)).observe(this, Observer { transactionData ->
                    transactionViewModel.isLoading.observe(this, Observer { loader ->
                        if(loader == false){
                            if (transactionData.first.isNotEmpty()) {
                                withdrawSum = 0
                                transactionData.first.forEachIndexed { _, element ->
                                    withdrawSum += Math.abs(element.transactionAttributes?.amount!!.toInt())
                                }
                                withdrawText.text = currencyData.symbol + " " + withdrawSum.toString()
                            } else {
                                // no withdrawal
                                withdrawSum = 0
                                withdrawText.text = currencyData.symbol + " " + "0"
                            }
                            if (transactionData.second.isNotEmpty()) {
                                depositSum = 0
                                transactionData.second.forEachIndexed { _, element ->
                                    depositSum += Math.abs(element.transactionAttributes?.amount!!.toInt())
                                }
                                incomeDigit.text = currencyData.symbol + " " + depositSum.toString()
                            } else {
                                // no deposit
                                depositSum = 0
                                incomeDigit.text = currencyData.symbol + " " + "0"
                            }
                            transaction = depositSum - withdrawSum
                            sumText.text = currencyData.symbol + " " + transaction.toString()
                        }
                    })
                })
            }
        })
    }

    private fun setBarChart() {
        monthText.text = DateTimeUtil.getCurrentMonth()
        val dataColor = arrayListOf<Int>()
        for (c in ColorTemplate.MATERIAL_COLORS) {
            dataColor.add(c)
        }
        currencyViewModel.getDefaultCurrency().observe(this, Observer { defaultCurrency ->
            if(defaultCurrency.isNotEmpty()) {
                val currencyData = defaultCurrency[0].currencyAttributes
                zipLiveData(budgetLimit.retrieveSpentBudget(),
                        budgetLimit.retrieveCurrentMonthBudget(currencyData?.code!!)).observe(this, Observer { budget ->
                    zipLiveData(budgetLimit.spentBudgetLoader, budgetLimit.currentMonthBudgetLoader).observe(this, Observer {
                        loading ->
                        if(loading.first == false && loading.second == false){
                            budgetSpent = budget.first.toFloat()
                            budgeted = budget.second.toFloat()
                            val budgetLeftPercentage = (budgetSpent / budgeted) * 100
                            val budgetSpentPercentage = (budgeted - budgetSpent) / budgeted * 100
                            val dataSet = PieDataSet(arrayListOf(PieEntry(budgetLeftPercentage, "Left"),
                                    PieEntry(budgetSpentPercentage, "Spent")), "").apply {
                                setDrawIcons(true)
                                sliceSpace = 2f
                                iconsOffset = MPPointF(0f, 40f)
                                colors = dataColor
                                valueTextSize = 15f
                            }
                            budgetAmount.text = currencyData.symbol + " " + budgeted
                            spentAmount.text = currencyData.symbol + " " + budgetSpent
                            budgetChart.apply {
                                data = PieData(dataSet)
                                description = Description().apply { text = "Budget Percentage" }
                                highlightValue(null)
                                invalidate()
                            }
                            val progressDrawable = budgetProgress.progressDrawable.mutate()
                            if (budgetLeftPercentage.roundToInt() >= 80) {
                                progressDrawable.setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN)
                                budgetProgress.progressDrawable = progressDrawable
                            } else if (budgetLeftPercentage.roundToInt() in 50..80) {
                                progressDrawable.setColorFilter(Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_IN)
                                budgetProgress.progressDrawable = progressDrawable
                            } else {
                                progressDrawable.setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN)
                                budgetProgress.progressDrawable = progressDrawable
                            }
                            ObjectAnimator.ofInt(budgetProgress, "progress", budgetLeftPercentage.roundToInt()).start()
                        }
                    })
                })
            }
        })
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = resources.getString(R.string.dashboard)
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = resources.getString(R.string.dashboard)
    }

}