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
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView
import me.toptas.fancyshowcase.FocusShape
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.budget.BudgetViewModel
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyAttributes
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.budget.BudgetSummaryFragment
import xyz.hisname.fireflyiii.ui.transaction.RecentTransactionFragment
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionActivity
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.LocaleNumberParser
import xyz.hisname.fireflyiii.util.MpAndroidPercentFormatter
import xyz.hisname.fireflyiii.util.extension.*
import kotlin.math.roundToInt


// TODO: Refactor this god class (7 Jan 2019)
class DashboardFragment: BaseFragment() {

    private val budgetLimit by lazy { getViewModel(BudgetViewModel::class.java) }
    private var depositSum = 0.toDouble()
    private var withdrawSum = 0.toDouble()
    private var transaction = 0.toDouble()
    private var budgetSpent = 0f
    private var budgeted = 0f
    private var month2Depot = 0.toDouble()
    private var month3Depot = 0.toDouble()
    private var month2With = 0.toDouble()
    private var month3With = 0.toDouble()

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
        currencyViewModel.getDefaultCurrency().observe(this, Observer { defaultCurrency ->
            if (defaultCurrency.isNotEmpty()) {
                val currencyData = defaultCurrency[0].currencyAttributes
                setNetIncome(currencyData)
                setPieChart(currencyData)
                setNetWorth(currencyData)
                setAverage(currencyData)
            }
        })
        currencyViewModel.apiResponse.observe(this, Observer {
            toastInfo(it)
        })
        fab.display {
            fab.isClickable = false
            requireActivity().startActivity(Intent(requireContext(), AddTransactionActivity::class.java))
            fab.isClickable = true
        }
        requireFragmentManager().commit {
            replace(R.id.recentTransactionCard, RecentTransactionFragment())
        }
        showHelpText()
    }

    private fun setNetWorth(currencyData: CurrencyAttributes?){
        val currencyCode = currencyData?.code!!
        accountViewModel.getAllAccountWithNetworthAndCurrency(currencyCode).observe(this, Observer { money ->
            accountViewModel.isLoading.observe(this, Observer { load ->
                if (load == false) {
                    netWorthText.text = currencyData.symbol + " " + money
                }
            })
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setNetIncome(currencyData: CurrencyAttributes?){
        val currencyCode = currencyData?.code!!
        zipLiveData(transactionViewModel.getWithdrawalAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), currencyCode), transactionViewModel.getDepositAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), currencyCode), transactionViewModel.getWithdrawalAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(1),
                DateTimeUtil.getEndOfMonth(1), currencyCode),
                transactionViewModel.getDepositAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(1),
                        DateTimeUtil.getEndOfMonth(1), currencyCode), transactionViewModel.getWithdrawalAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(2),
                DateTimeUtil.getEndOfMonth(2), currencyCode),
                transactionViewModel.getDepositAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(2),
                        DateTimeUtil.getEndOfMonth(2), currencyCode)).observe(this, Observer { transactionData ->

            withdrawSum = transactionData.first
            depositSum = transactionData.second
            month2With = transactionData.third
            month2Depot = transactionData.fourth
            month3With = transactionData.fifth
            month3Depot = transactionData.sixth
            currentExpense.text = currencyData.symbol + " " + withdrawSum.toString()
            currentMonthIncome.text = currencyData.symbol + " " + depositSum.toString()
            transaction = depositSum - withdrawSum
            if(Math.copySign(1.toDouble(), transaction) < 0){
                currentNetIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_red_700))
            }
            balanceText.text = currencyData.symbol + " " + LocaleNumberParser.parseDecimal(transaction, requireContext())
            currentNetIncome.text = currencyData.symbol + " " + LocaleNumberParser.parseDecimal(transaction, requireContext())

            transaction = month2Depot - month2With
            oneMonthBeforeExpense.text = currencyData.symbol + " " + month2With.toString()
            oneMonthBeforeIncome.text = currencyData.symbol + " " + month2Depot.toString()
            if(Math.copySign(1.toDouble(), transaction) < 0){
                oneMonthBeforeNetIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_red_700))
            }
            oneMonthBeforeNetIncome.text = currencyData.symbol + " " + LocaleNumberParser.parseDecimal(transaction, requireContext())

            transaction = month3Depot - month3With
            twoMonthBeforeExpense.text = currencyData.symbol + " " + month3With.toString()
            twoMonthBeforeIncome.text = currencyData.symbol + " " + month3Depot.toString()
            if(Math.copySign(1.toDouble(), transaction) < 0){
                twoMonthBeforeNetIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_red_700))
            }
            twoMonthBeforeNetIncome.text = currencyData.symbol + " " + LocaleNumberParser.parseDecimal(transaction, requireContext())

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
        })
    }

    private fun setAverage(currencyData: CurrencyAttributes?){
        val currencyCode = currencyData?.code!!
        zipLiveData(transactionViewModel.getWithdrawalAmountWithCurrencyCode(
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
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6), currencyCode)).observe(this, Observer { transactionData ->
            transactionViewModel.isLoading.observe(this, Observer { loader ->
                if(loader == false) {
                    val firstDay = transactionData.first
                    val secondDay = transactionData.second
                    val thirdDay = transactionData.third
                    val fourthDay = transactionData.fourth
                    val fifthDay = transactionData.fifth
                    val sixthDay = transactionData.sixth
                    val sixDayAverage = (firstDay + secondDay + thirdDay + fourthDay + fifthDay + sixthDay).div(6)
                    sixDaysAverage.text = currencyData.symbol + LocaleNumberParser.parseDecimal(sixDayAverage, requireContext())
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
                }
            })
        })
        transactionViewModel.getWithdrawalAmountWithCurrencyCode(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 30),
                DateTimeUtil.getTodayDate(), currencyCode).observe(this, Observer { transactionData ->
            transactionViewModel.isLoading.observe(this, Observer { loader ->
                if(loader == false) {
                    thirtyDaysAverage.text = currencyData.symbol + LocaleNumberParser.parseDecimal(
                            transactionData.div(30), requireContext())
                }
            })
        })
    }

    private fun setPieChart(currencyData: CurrencyAttributes?) {
        monthText.text = DateTimeUtil.getCurrentMonth()
        val dataColor = arrayListOf(ContextCompat.getColor(requireContext(), R.color.md_red_700), ContextCompat.getColor(requireContext(), R.color.md_green_500))
        zipLiveData(budgetLimit.retrieveSpentBudget(),
                budgetLimit.retrieveCurrentMonthBudget(currencyData?.code!!)).observe(this, Observer { budget ->
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
                valueFormatter = MpAndroidPercentFormatter()
            }
            budgetAmount.text = currencyData.symbol + " " + budgeted
            spentAmount.text = currencyData.symbol + " " + budgetSpent
            budgetChart.apply {
                data = PieData(dataSet)
                description.text = "Budget Percentage"
                highlightValue(null)
                isDrawHoleEnabled = false
            }
            val progressDrawable = budgetProgress.progressDrawable.mutate()

            leftToSpentText.text = currencyData.symbol + " " +
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
        })
        budgetCard.setOnClickListener {
            requireFragmentManager().commit {
                replace(R.id.fragment_container, BudgetSummaryFragment())
                addToBackStack(null)
            }
        }
        budgetChart.setOnClickListener {
            requireFragmentManager().commit {
                replace(R.id.fragment_container, BudgetSummaryFragment())
                addToBackStack(null)
            }
        }
    }

    private fun changeTheme(){
        if (isDarkMode()){
            netEarningsExtraInfoLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
            netEarningsChart.legend.textColor = ContextCompat.getColor(requireContext(), R.color.white)
            netEarningsChart.axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.white)
            netEarningsChart.axisRight.textColor = ContextCompat.getColor(requireContext(), R.color.white)
            netEarningsChart.xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.white)
            dailySummaryExtraInfoLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
            dailySummaryChart.legend.textColor = ContextCompat.getColor(requireContext(), R.color.white)
            dailySummaryChart.axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.white)
            dailySummaryChart.axisRight.textColor = ContextCompat.getColor(requireContext(), R.color.white)
            dailySummaryChart.xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.white)
            budgetExtraInfoLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
            budgetChart.legend.textColor = ContextCompat.getColor(requireContext(), R.color.white)
            budgetChart.description.textColor = ContextCompat.getColor(requireContext(), R.color.white)
        }
    }

    private fun showHelpText(){
        val enterAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_from_left)
        val balanceLayoutCaseView = FancyShowCaseView.Builder(requireActivity())
                .focusOn(balanceLayout)
                .title(resources.getString(R.string.dashboard_balance_help_text))
                .enableAutoTextPosition()
                .showOnce("balanceLayoutCaseView")
                .fitSystemWindows(true)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .enterAnimation(enterAnimation)
                .closeOnTouch(true)

        FancyShowCaseQueue()
                .add(balanceLayoutCaseView.build()).show()
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