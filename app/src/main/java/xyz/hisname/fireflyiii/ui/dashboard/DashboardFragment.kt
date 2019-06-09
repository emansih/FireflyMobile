package xyz.hisname.fireflyiii.ui.dashboard

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.isGone
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.budget.BudgetViewModel
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyAttributes
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.budget.BudgetSummaryFragment
import xyz.hisname.fireflyiii.ui.transaction.RecentTransactionFragment
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionActivity
import xyz.hisname.fireflyiii.util.*
import xyz.hisname.fireflyiii.util.extension.*
import kotlin.math.roundToInt


// TODO: Refactor this god class (7 Jan 2019)
class DashboardFragment: BaseFragment() {

    private val budgetLimit by lazy { getViewModel(BudgetViewModel::class.java) }
    private var depositSum = 0.0
    private var withdrawSum = 0.0
    private var transaction = 0.0
    private var budgetSpent = 0f
    private var budgeted = 0f
    private var month2Depot = 0.0
    private var month3Depot = 0.0
    private var month2With = 0.0
    private var month3With = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_dashboard,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        twoMonthBefore.text = DateTimeUtil.getPreviousMonthShortName(2)
        oneMonthBefore.text = DateTimeUtil.getPreviousMonthShortName(1)
        currentMonthTextView.text = DateTimeUtil.getCurrentMonthShortName()
        changeTheme()
        currencyViewModel.getDefaultCurrency().observe(this) { defaultCurrency ->
            val currencyData = defaultCurrency[0].currencyAttributes
            getTransactionData(currencyData)
            setPieChart(currencyData)
            setNetWorth(currencyData)
        }
        animateCard(statsCard, netEarningsCard, dailySummaryCard, recentTransactionCard, budgetCard)
        currencyViewModel.apiResponse.observe(this){
            toastInfo(it)
        }
        fab.display {
            fab.isClickable = false
            requireActivity().startActivity(Intent(requireContext(), AddTransactionActivity::class.java))
            fab.isClickable = true
        }
        requireFragmentManager().commit {
            replace(R.id.recentTransactionCard, RecentTransactionFragment())
        }
        budgetCard.setOnClickListener {
            requireFragmentManager().commit {
                replace(R.id.fragment_container, BudgetSummaryFragment())
                addToBackStack(null)
            }
        }
    }

    private fun setNetWorth(currencyData: CurrencyAttributes?){
        val currencyCode = currencyData?.code ?: ""
        accountViewModel.getAllAccountWithNetworthAndCurrency(currencyCode).observe(this) { money ->
            netWorthText.text = currencyData?.symbol + money
        }
    }

    private fun getTransactionData(currencyData: CurrencyAttributes?){
        val currencyCode = currencyData?.code ?: ""
        zipLiveData(zipLiveData(transactionViewModel.getWithdrawalAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), currencyCode), transactionViewModel.getDepositAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), currencyCode), transactionViewModel.getWithdrawalAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(1),
                DateTimeUtil.getEndOfMonth(1), currencyCode),
                transactionViewModel.getDepositAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(1),
                        DateTimeUtil.getEndOfMonth(1), currencyCode), transactionViewModel.getWithdrawalAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(2),
                DateTimeUtil.getEndOfMonth(2), currencyCode),
                transactionViewModel.getDepositAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(2),
                        DateTimeUtil.getEndOfMonth(2), currencyCode)), zipLiveData(transactionViewModel.getWithdrawalAmountWithCurrencyCode(
                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 1),
                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 1), currencyCode),
                transactionViewModel.getWithdrawalAmountWithCurrencyCode(
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 2),
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 2), currencyCode),
                transactionViewModel.getWithdrawalAmountWithCurrencyCode(
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 3),
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 3), currencyCode),
                transactionViewModel.getWithdrawalAmountWithCurrencyCode(
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 4),
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 4), currencyCode),
                transactionViewModel.getWithdrawalAmountWithCurrencyCode(
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 5),
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 5), currencyCode),
                transactionViewModel.getWithdrawalAmountWithCurrencyCode(
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6), currencyCode))).observe(this) { transactionData ->
            setNetIncome(currencyData?.symbol ?: "", transactionData.first)
            setAverage(currencyData?.symbol ?: "", currencyCode, transactionData.second)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setNetIncome(currencySymbol: String, transactionData: Sixple<Double,Double,Double,Double,Double,Double>){
            withdrawSum = transactionData.first
            depositSum = transactionData.second
            month2With = transactionData.third
            month2Depot = transactionData.fourth
            month3With = transactionData.fifth
            month3Depot = transactionData.sixth
            currentExpense.text = currencySymbol + withdrawSum.toString()
            currentMonthIncome.text = currencySymbol + depositSum.toString()
            transaction = depositSum - withdrawSum
            if(Math.copySign(1.toDouble(), transaction) < 0){
                currentNetIncome.setTextColor(getCompatColor(R.color.md_red_700))
            }
            balanceText.text = currencySymbol + LocaleNumberParser.parseDecimal(transaction, requireContext())
            currentNetIncome.text = currencySymbol + " " + LocaleNumberParser.parseDecimal(transaction, requireContext())

            transaction = month2Depot - month2With
            oneMonthBeforeExpense.text = currencySymbol + month2With.toString()
            oneMonthBeforeIncome.text = currencySymbol + month2Depot.toString()
            if(Math.copySign(1.toDouble(), transaction) < 0){
                oneMonthBeforeNetIncome.setTextColor(getCompatColor(R.color.md_red_700))
            }
            oneMonthBeforeNetIncome.text = currencySymbol + LocaleNumberParser.parseDecimal(transaction, requireContext())

            transaction = month3Depot - month3With
            twoMonthBeforeExpense.text = currencySymbol + month3With.toString()
            twoMonthBeforeIncome.text = currencySymbol + month3Depot.toString()
            if(Math.copySign(1.toDouble(), transaction) < 0){
                twoMonthBeforeNetIncome.setTextColor(getCompatColor(R.color.md_red_700))
            }
            twoMonthBeforeNetIncome.text = currencySymbol + LocaleNumberParser.parseDecimal(transaction, requireContext())

            val withDrawalHistory = arrayListOf(
                    BarEntry(0f, month3With.toFloat()),
                    BarEntry(1f, month2With.toFloat()),
                    BarEntry(2f, withdrawSum.toFloat()))
            val depositHistory = arrayListOf(
                    BarEntry(0f, month3Depot.toFloat()),
                    BarEntry(1f, month2Depot.toFloat()),
                    BarEntry(2f, depositSum.toFloat()))

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
                xAxis.setCenterAxisLabels(true)
                data.isHighlightEnabled = false
                animateY(1000)
            }
    }

    private fun setAverage(currencySymbol: String, currencyCode: String, transactionData: Sixple<Double,Double,Double,Double,Double,Double>){
        val firstDay = transactionData.first
        val secondDay = transactionData.second
        val thirdDay = transactionData.third
        val fourthDay = transactionData.fourth
        val fifthDay = transactionData.fifth
        val sixthDay = transactionData.sixth
        val sixDayAverage = (firstDay + secondDay + thirdDay + fourthDay + fifthDay + sixthDay).div(6)
        sixDaysAverage.text = currencySymbol + LocaleNumberParser.parseDecimal(sixDayAverage, requireContext())
        val expenseHistory = arrayListOf(
                BarEntry(0f, firstDay.toFloat()),
                BarEntry(1f, secondDay.toFloat()),
                BarEntry(2f, thirdDay.toFloat()),
                BarEntry(3f, fourthDay.toFloat()),
                BarEntry(4f, fifthDay.toFloat()),
                BarEntry(5f, sixthDay.toFloat())
        )
        val expenseSet = BarDataSet(expenseHistory, resources.getString(R.string.expense))
        expenseSet.apply {
            valueTextColor = Color.RED
            color = Color.RED
            valueTextSize = 15f
        }
        dailySummaryChart.apply {
            description.isEnabled = false
            isScaleXEnabled = false
            setDrawBarShadow(false)
            setDrawGridBackground(false)
            xAxis.valueFormatter = IndexAxisValueFormatter(arrayListOf(
                    DateTimeUtil.getDayAndMonth(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(),1)),
                    DateTimeUtil.getDayAndMonth((DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 2))),
                    DateTimeUtil.getDayAndMonth((DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 3))),
                    DateTimeUtil.getDayAndMonth((DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 4))),
                    DateTimeUtil.getDayAndMonth((DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 5))),
                    DateTimeUtil.getDayAndMonth((DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6)))))
            data = BarData(expenseSet)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            data.isHighlightEnabled = false
            animateY(1000)
            setTouchEnabled(true)
        }
        transactionViewModel.getWithdrawalAmountWithCurrencyCode(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 30),
                DateTimeUtil.getTodayDate(), currencyCode).observe(this){ transaction ->
            thirtyDaysAverage.text = currencySymbol + LocaleNumberParser.parseDecimal(
                    transaction.div(30), requireContext())

        }
    }

    private fun setPieChart(currencyData: CurrencyAttributes?) {
        monthText.text = DateTimeUtil.getCurrentMonth()
        val dataColor = arrayListOf(getCompatColor(R.color.md_red_700),
                getCompatColor(R.color.md_green_500))
        zipLiveData(budgetLimit.retrieveSpentBudget(currencyData?.code ?: ""),
                budgetLimit.retrieveCurrentMonthBudget(currencyData?.code ?: "")).observe(this) { budget ->
            budgetSpent = budget.first.toFloat()
            budgeted = budget.second.toFloat()
            val budgetLeftPercentage = (budgetSpent / budgeted) * 100
            val budgetSpentPercentage = (budgeted - budgetSpent) / budgeted * 100
            val dataSet = PieDataSet(arrayListOf(PieEntry(budgetLeftPercentage,
                    resources.getString(R.string.spent)), PieEntry(budgetSpentPercentage,
                    resources.getString(R.string.left_to_spend))), "").apply {
                setDrawIcons(true)
                sliceSpace = 2f
                iconsOffset = MPPointF(0f, 40f)
                colors = dataColor
                valueTextSize = 15f
                valueFormatter = PercentFormatter()
            }
            budgetAmount.text = currencyData?.symbol + budgeted
            spentAmount.text = currencyData?.symbol + budgetSpent
            budgetChart.setData {
                data = PieData(dataSet)
                description.text = "Budget Percentage"
                setOnClickListener {
                    requireFragmentManager().commit {
                        replace(R.id.fragment_container, BudgetSummaryFragment())
                        addToBackStack(null)
                    }
                }
            }
            val progressDrawable = budgetProgress.progressDrawable.mutate()

            leftToSpentText.text = currencyData?.symbol +
                    LocaleNumberParser.parseDecimal((budgeted - budgetSpent).toDouble(), requireContext())
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
    }

    private fun changeTheme(){
        if (isDarkMode()){
            netEarningsExtraInfoLayout.setBackgroundColor(getCompatColor(R.color.md_black_1000))
            netEarningsChart.legend.textColor = getCompatColor(R.color.white)
            netEarningsChart.axisLeft.textColor = getCompatColor(R.color.white)
            netEarningsChart.axisRight.textColor = getCompatColor(R.color.white)
            netEarningsChart.xAxis.textColor = getCompatColor(R.color.white)
            dailySummaryExtraInfoLayout.setBackgroundColor(getCompatColor(R.color.md_black_1000))
            dailySummaryChart.legend.textColor = getCompatColor(R.color.white)
            dailySummaryChart.axisLeft.textColor = getCompatColor(R.color.white)
            dailySummaryChart.axisRight.textColor = getCompatColor(R.color.white)
            dailySummaryChart.xAxis.textColor = getCompatColor(R.color.white)
            budgetExtraInfoLayout.setBackgroundColor(getCompatColor(R.color.md_black_1000))
        }
    }

    private fun animateCard(vararg frameLayout: FrameLayout){
        for(frames in frameLayout){
            frames.translationY = getScreenHeight().toFloat()
            frames.animate()
                    .translationY(0f)
                    .setInterpolator(DecelerateInterpolator(5f))
                    .setDuration(3000)
                    .withEndAction {
                        if(frames == budgetCard){
                            val helpText = showCase(R.string.dashboard_balance_help_text,
                                    "balanceLayoutCaseView", balanceLayout)
                            helpText.show()
                        }
                    }
        }
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

    override fun handleBack() {
        requireActivity().finish()
    }
}