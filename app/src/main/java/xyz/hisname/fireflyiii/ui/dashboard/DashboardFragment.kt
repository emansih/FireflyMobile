package xyz.hisname.fireflyiii.ui.dashboard

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.account.AccountsViewModel
import xyz.hisname.fireflyiii.repository.budget.BudgetViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyViewModel
import xyz.hisname.fireflyiii.repository.transaction.TransactionsViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.transaction.AddTransactionFragment
import xyz.hisname.fireflyiii.ui.transaction.RecentTransactionFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import kotlin.math.roundToInt


// TODO: Refactor this god class (7 Jan 2019)
class DashboardFragment: BaseFragment() {

    private val transactionViewModel by lazy { getViewModel(TransactionsViewModel::class.java) }
    private val currencyViewModel by lazy { getViewModel(CurrencyViewModel::class.java) }
    private val accountViewModel by lazy { getViewModel(AccountsViewModel::class.java) }
    private val budgetLimit by lazy { getViewModel(BudgetViewModel::class.java) }
    private val fab by lazy { requireActivity().findViewById<FloatingActionButton>(R.id.globalFAB) }
    private var depositSum = 0.toBigDecimal()
    private var withdrawSum = 0.toBigDecimal()
    private var transaction = 0.toBigDecimal()
    private var budgetSpent = 0f
    private var budgeted = 0f
    private var month2Depot = 0.toBigDecimal()
    private var month3Depot = 0.toBigDecimal()
    private var month2With = 0.toBigDecimal()
    private var month3With = 0.toBigDecimal()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_dashboard,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        twoMonthBefore.text = DateTimeUtil.getPreviousMonthShortName(2)
        oneMonthBefore.text = DateTimeUtil.getPreviousMonthShortName(1)
        currentMonthTextView.text = DateTimeUtil.getCurrentMonthShortName()
        setNetIncome()
        setBarChart()
        setNetWorth()
        currencyViewModel.apiResponse.observe(this, Observer {
            toastInfo(it)
        })
        fab.display {
            fab.isClickable = false
            requireFragmentManager().commit {
                val addTransactionFragment = AddTransactionFragment()
                addTransactionFragment.arguments = bundleOf("revealX" to fab.width / 2, "revealY" to fab.height / 2)
                replace(R.id.bigger_fragment_container, addTransactionFragment)
                addToBackStack(null)
            }
            fab.isClickable = true
        }
        if(requireFragmentManager().findFragmentByTag("fragmentTransaction") is AddTransactionFragment) {
            fab.isGone = true
        }
    }

    private fun setNetWorth(){
        currencyViewModel.getDefaultCurrency().observe(this, Observer { defaultCurrency ->
            if(defaultCurrency.isNotEmpty()) {
                val currencyData = defaultCurrency[0].currencyAttributes
                val currencyCode = currencyData?.code!!
                accountViewModel.getAllAccountWithNetworthAndCurrency(currencyCode).observe(this, Observer { money ->
                    accountViewModel.isLoading.observe(this, Observer { load ->
                        if(load == false){
                            netWorthText.text = currencyData.symbol + " " + money
                        }
                    })
                })
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setNetIncome(){
        currencyViewModel.getDefaultCurrency().observe(this, Observer { defaultCurrency ->
            if(defaultCurrency.isNotEmpty()) {
                val currencyData = defaultCurrency[0].currencyAttributes
                val currencyCode = currencyData?.code!!
                zipLiveData(transactionViewModel.getWithdrawalAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(),
                        DateTimeUtil.getEndOfMonth(), currencyCode),
                        transactionViewModel.getDepositAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(),
                                DateTimeUtil.getEndOfMonth(), currencyCode)).observe(this, Observer { transactionData ->
                    transactionViewModel.isLoading.observe(this, Observer { loader ->
                        if(loader == false){
                            depositSum = transactionData.second
                            withdrawSum = transactionData.first
                            transaction = depositSum - withdrawSum
                            currentExpense.text = currencyData.symbol + " " + withdrawSum.toString()
                            currentMonthIncome.text = currencyData.symbol + " " + depositSum.toString()
                            if(transaction.signum() == -1){
                                currentNetIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_red_700))
                            }
                            balanceText.text = currencyData.symbol + " " + transaction.toString()
                            currentNetIncome.text = currencyData.symbol + " " + transaction.toString()
                        }
                    })
                })
                zipLiveData(transactionViewModel.getWithdrawalAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(1),
                        DateTimeUtil.getEndOfMonth(1), currencyCode),
                        transactionViewModel.getDepositAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(1),
                                DateTimeUtil.getEndOfMonth(1), currencyCode)).observe(this, Observer { transactionData ->
                    transactionViewModel.isLoading.observe(this, Observer { loader ->
                        if(loader == false){
                            month2Depot = transactionData.second
                            month2With = transactionData.first
                            transaction = month2Depot - month2With
                            oneMonthBeforeExpense.text = currencyData.symbol + " " + month2With.toString()
                            oneMonthBeforeIncome.text = currencyData.symbol + " " + month2Depot.toString()
                            if(transaction.signum() == -1){
                                oneMonthBeforeNetIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_red_700))
                            }
                            oneMonthBeforeNetIncome.text = currencyData.symbol + " " + transaction.toString()
                        }
                    })
                })

                zipLiveData(transactionViewModel.getWithdrawalAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(2),
                        DateTimeUtil.getEndOfMonth(2), currencyCode),
                        transactionViewModel.getDepositAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(2),
                                DateTimeUtil.getEndOfMonth(2), currencyCode)).observe(this, Observer { transactionData ->
                    transactionViewModel.isLoading.observe(this, Observer { loader ->
                        if(loader == false){
                            month3Depot = transactionData.second
                            month3With = transactionData.first
                            transaction = month3Depot - month3With
                            twoMonthBeforeExpense.text = currencyData.symbol + " " + month3With.toString()
                            twoMonthBeforeIncome.text = currencyData.symbol + " " + month3Depot.toString()
                            if(transaction.signum() == -1){
                                twoMonthBeforeNetIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_red_700))
                            }
                            twoMonthBeforeNetIncome.text = currencyData.symbol + " " + transaction.toString()
                        }
                    })
                })
                val withDrawalHistory = arrayListOf(
                        BarEntry(month3With.toFloat(), month3With.toFloat()),
                        BarEntry(month2With.toFloat(), month2With.toFloat()),
                        BarEntry(withdrawSum.toFloat(), withdrawSum.toFloat()))
                val depositHistory = arrayListOf(
                        BarEntry(month3Depot.toFloat(), month3Depot.toFloat()),
                        BarEntry(month2Depot.toFloat(), month2Depot.toFloat()),
                        BarEntry(depositSum.toFloat(), depositSum.toFloat()))
                val withDrawalSets = BarDataSet(withDrawalHistory, resources.getString(R.string.withdrawal))
                val depositSets = BarDataSet(depositHistory, resources.getString(R.string.deposit))
                depositSets.apply {
                    valueFormatter = LargeValueFormatter()
                    valueTextColor = Color.GREEN
                    color = Color.GREEN
                    valueTextSize = 12f
                }
                withDrawalSets.apply {
                    valueTextColor = Color.RED
                    color = Color.RED
                    valueFormatter = LargeValueFormatter()
                    valueTextSize = 12f
                }
                netEarningsChart.apply {
                    description.isEnabled = false
                    isScaleXEnabled = false
                    setDrawBarShadow(false)
                    setDrawGridBackground(false)
                    xAxis.valueFormatter = IndexAxisValueFormatter(arrayListOf(DateTimeUtil.getPreviousMonthShortName(2),
                            DateTimeUtil.getPreviousMonthShortName(1),
                            DateTimeUtil.getCurrentMonthShortName()))
                    data = BarData(depositSets, withDrawalSets)
                    barData.barWidth = 0.3f
                    xAxis.axisMaximum = netEarningsChart.barData.getGroupWidth(0.4f, 0f) * 3
                    groupBars(0f, 0.4f, 0f)
                    data.isHighlightEnabled = false
                    animateY(1000)
                    setTouchEnabled(true)
                    invalidate()
                }
                // For some reason fragment transaction needs to be done here. Calling at some
                // other places will cause the dashboard to scroll to some weird spot....
                requireFragmentManager().commit {
                    replace(R.id.recentTransactionCard, RecentTransactionFragment())
                }
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
                            leftToSpentText.text = currencyData.symbol + " " + String.format("%.2f",(budgeted - budgetSpent))
                            if(!budgetLeftPercentage.isNaN()) {
                                when {
                                    budgetLeftPercentage.roundToInt() >= 80 -> {
                                        progressDrawable.setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN)
                                        budgetProgress.progressDrawable = progressDrawable
                                    }
                                    budgetLeftPercentage.roundToInt() in 50..80 -> {
                                        progressDrawable.setColorFilter(Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_IN)
                                        budgetProgress.progressDrawable = progressDrawable
                                    }
                                    else -> {
                                        progressDrawable.setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN)
                                        budgetProgress.progressDrawable = progressDrawable
                                    }
                                }
                                ObjectAnimator.ofInt(budgetProgress, "progress", budgetLeftPercentage.roundToInt()).start()
                            }
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

    override fun onDetach() {
        super.onDetach()
        fab.isGone = true
    }
}