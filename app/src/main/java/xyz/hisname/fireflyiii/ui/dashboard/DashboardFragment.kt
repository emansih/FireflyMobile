package xyz.hisname.fireflyiii.ui.dashboard

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.budget.BudgetViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyViewModel
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyAttributes
import xyz.hisname.fireflyiii.repository.summary.SummaryViewModel
import xyz.hisname.fireflyiii.repository.transaction.TransactionsViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.budget.BudgetSummaryFragment
import xyz.hisname.fireflyiii.ui.transaction.RecentTransactionFragment
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionActivity
import xyz.hisname.fireflyiii.ui.widgets.BalanceWidget
import xyz.hisname.fireflyiii.ui.widgets.BillsToPayWidget
import xyz.hisname.fireflyiii.util.*
import xyz.hisname.fireflyiii.util.extension.*
import java.math.BigDecimal
import kotlin.math.roundToInt
import kotlin.math.withSign


// TODO: Refactor this god class (7 Jan 2019)
class DashboardFragment: BaseFragment() {

    private val budgetLimit by lazy { getImprovedViewModel(BudgetViewModel::class.java) }
    private val summaryViewModel by lazy { getImprovedViewModel(SummaryViewModel::class.java) }
    private val transactionExtendedFab by bindView<ExtendedFloatingActionButton>(R.id.addTransactionExtended)
    private val currencyViewModel by lazy { getImprovedViewModel(CurrencyViewModel::class.java)}
    private val transactionVM by lazy { getImprovedViewModel(TransactionsViewModel::class.java) }

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
        animateCard(balanceCard, billsCard, netEarningsCard, dailySummaryCard,
                leftToSpendCard, networthCard, recentTransactionCard, budgetCard)
        twoMonthBefore.text = DateTimeUtil.getPreviousMonthShortName(2)
        oneMonthBefore.text = DateTimeUtil.getPreviousMonthShortName(1)
        currentMonthTextView.text = DateTimeUtil.getCurrentMonthShortName()
        changeTheme()
        balanceCard.layoutParams.width = (getScreenWidth() - 425)
        billsCard.layoutParams.width = (getScreenWidth() - 425)
        leftToSpendCard.layoutParams.width = (getScreenWidth() - 425)
        networthCard.layoutParams.width = (getScreenWidth() - 425)
        currencyViewModel.getDefaultCurrency().observe(viewLifecycleOwner) { defaultCurrency ->
            val currencyData = defaultCurrency[0].currencyAttributes
            setSummary(currencyData?.code ?: "")
            setPieChart(currencyData)
            getTransactionData(currencyData)

        }
        setExtendedFab()
        parentFragmentManager.commit {
            replace(R.id.recentTransactionCard, RecentTransactionFragment())
        }
        budgetCard.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, BudgetSummaryFragment())
                addToBackStack(null)
            }
        }
        setIcon()
    }

    private fun setExtendedFab(){
        transactionExtendedFab.isVisible = true
        dashboardNested.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY) {
                transactionExtendedFab.shrink()
            } else {
                transactionExtendedFab.extend()
            }
        }
        transactionExtendedFab.setOnClickListener {
            requireActivity().startActivity(Intent(requireContext(), AddTransactionActivity::class.java))
        }
    }

    private fun setSummary(currencyCode: String){
        summaryViewModel.getBasicSummary(DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(),
                currencyCode)

        summaryViewModel.networthValue.observe(viewLifecycleOwner){ money ->
             networthAmount.text = money
        }

        summaryViewModel.leftToSpendValue.observe(viewLifecycleOwner){ money ->
            leftToSpendAmountText.text = money
        }
        summaryViewModel.balanceValue.observe(viewLifecycleOwner){ money ->
            balanceText.text = money
            updateHomeScreenWidget(BalanceWidget::class.java)
        }
        summaryViewModel.earnedValue.observe(viewLifecycleOwner){ money ->
            balanceEarnedText.text = money + " + "
        }
        summaryViewModel.spentValue.observe(viewLifecycleOwner){ money ->
            balanceSpentText.text = money
        }
        summaryViewModel.billsToPay.observe(viewLifecycleOwner){ money ->
            billsText.text = money
            updateHomeScreenWidget(BillsToPayWidget::class.java)
        }

        summaryViewModel.billsPaid.observe(viewLifecycleOwner){ money ->
            billsPaidText.text = money
        }

        summaryViewModel.leftToSpendDay.observe(viewLifecycleOwner){ money ->
            leftToSpendAmount.text = money
        }
    }

    private fun updateHomeScreenWidget(className: Class<*>){
        val updateIntent = Intent(requireContext(), className)
        updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(requireContext())
                .getAppWidgetIds(ComponentName(requireContext(), className))
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        requireActivity().sendBroadcast(updateIntent)
    }

    private fun setIcon(){
        balanceIcon.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_balance_scale
            colorRes = R.color.md_white_1000
            sizeDp = 32
        })
        billsIcon.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_calendar
            colorRes =R.color.md_white_1000
            sizeDp = 32
        })
        leftToSpendIcon.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_money_bill
            colorRes = R.color.md_white_1000
            sizeDp = 32
        })
        networthIcon.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_chart_line
            colorRes = R.color.md_white_1000
            sizeDp = 32
        })
    }

    private fun getTransactionData(currencyData: CurrencyAttributes?){
        val currencyCode = currencyData?.code ?: ""
        zipLiveData(zipLiveData(transactionVM.getWithdrawalAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), currencyCode), transactionVM.getDepositAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), currencyCode), transactionVM.getWithdrawalAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(1),
                DateTimeUtil.getEndOfMonth(1), currencyCode),
                transactionVM.getDepositAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(1),
                        DateTimeUtil.getEndOfMonth(1), currencyCode), transactionVM.getWithdrawalAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(2),
                DateTimeUtil.getEndOfMonth(2), currencyCode),
                transactionVM.getDepositAmountWithCurrencyCode(DateTimeUtil.getStartOfMonth(2),
                        DateTimeUtil.getEndOfMonth(2), currencyCode)), zipLiveData(transactionVM.getWithdrawalAmountWithCurrencyCode(
                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 1),
                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 1), currencyCode),
                transactionVM.getWithdrawalAmountWithCurrencyCode(
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 2),
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 2), currencyCode),
                transactionVM.getWithdrawalAmountWithCurrencyCode(
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 3),
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 3), currencyCode),
                transactionVM.getWithdrawalAmountWithCurrencyCode(
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 4),
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 4), currencyCode),
                transactionVM.getWithdrawalAmountWithCurrencyCode(
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 5),
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 5), currencyCode),
                transactionVM.getWithdrawalAmountWithCurrencyCode(
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6), currencyCode))).observe(viewLifecycleOwner) { transactionData ->
            setNetIncome(currencyData?.symbol ?: "", transactionData.first)
            setAverage(currencyData?.symbol ?: "", currencyCode, transactionData.second)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setNetIncome(currencySymbol: String, transactionData: Sixple<BigDecimal,BigDecimal,BigDecimal,BigDecimal,BigDecimal,BigDecimal>){
        withdrawSum = transactionData.first
        depositSum = transactionData.second
        month2With = transactionData.third
        month2Depot = transactionData.fourth
        month3With = transactionData.fifth
        month3Depot = transactionData.sixth
        currentExpense.text = currencySymbol + withdrawSum.toString()
        currentMonthIncome.text = currencySymbol + depositSum.toString()
        transaction = depositSum - withdrawSum
        if(transaction < 0.toBigDecimal()){
            currentNetIncome.setTextColor(getCompatColor(R.color.md_red_700))
        }
        currentNetIncome.text = currencySymbol + " " + transaction

        transaction = month2Depot - month2With
        oneMonthBeforeExpense.text = currencySymbol + month2With.toString()
        oneMonthBeforeIncome.text = currencySymbol + month2Depot.toString()

        if(transaction < 0.toBigDecimal()){
            oneMonthBeforeNetIncome.setTextColor(getCompatColor(R.color.md_red_700))
        }
        oneMonthBeforeNetIncome.text = currencySymbol + transaction

        transaction = month3Depot - month3With
        twoMonthBeforeExpense.text = currencySymbol + month3With.toString()
        twoMonthBeforeIncome.text = currencySymbol + month3Depot.toString()
        if(transaction < 0.toBigDecimal()){
            twoMonthBeforeNetIncome.setTextColor(getCompatColor(R.color.md_red_700))
        }
        twoMonthBeforeNetIncome.text = currencySymbol + transaction

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

    private fun setAverage(currencySymbol: String, currencyCode: String, transactionData: Sixple<BigDecimal,BigDecimal,BigDecimal,BigDecimal,BigDecimal,BigDecimal>){
        val firstDay = transactionData.first
        val secondDay = transactionData.second
        val thirdDay = transactionData.third
        val fourthDay = transactionData.fourth
        val fifthDay = transactionData.fifth
        val sixthDay = transactionData.sixth
        val sixDayAverage = (firstDay + secondDay + thirdDay + fourthDay + fifthDay + sixthDay).div(6.toBigDecimal())
        sixDaysAverage.text = currencySymbol + sixDayAverage
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
        transactionVM.getWithdrawalAmountWithCurrencyCode(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 30),
                DateTimeUtil.getTodayDate(), currencyCode).observe(viewLifecycleOwner){ transaction ->
            thirtyDaysAverage.text = currencySymbol +
                    transaction.div(30.toBigDecimal())

        }
    }

    private fun setPieChart(currencyData: CurrencyAttributes?) {
        monthText.text = DateTimeUtil.getCurrentMonth()
        val dataColor = arrayListOf(getCompatColor(R.color.md_red_700),
                getCompatColor(R.color.md_green_500))
        zipLiveData(budgetLimit.retrieveSpentBudget(currencyData?.code ?: ""),
                budgetLimit.retrieveCurrentMonthBudget(currencyData?.code ?: "")).observe(viewLifecycleOwner) { budget ->
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
                valueFormatter = PercentFormatter(budgetChart)
            }
            budgetAmount.text = currencyData?.symbol + budgeted
            spentAmount.text = currencyData?.symbol + budgetSpent
            budgetChart.setData {
                data = PieData(dataSet)
                description.isEnabled = false
                setUsePercentValues(true)
                setOnClickListener {
                    parentFragmentManager.commit {
                        replace(R.id.fragment_container, BudgetSummaryFragment())
                        addToBackStack(null)
                    }
                }
            }
            val progressDrawable = budgetProgress.progressDrawable.mutate()
            if(!budgetLeftPercentage.isNaN()) {
                when {
                    budgetLeftPercentage.roundToInt() >= 80 -> {
                        progressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.RED, BlendModeCompat.SRC_ATOP)
                        budgetProgress.progressDrawable = progressDrawable
                    }
                    budgetLeftPercentage.roundToInt() in 50..80 -> {
                        progressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.YELLOW, BlendModeCompat.SRC_ATOP)
                        budgetProgress.progressDrawable = progressDrawable
                    }
                    else -> {
                        progressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.GREEN, BlendModeCompat.SRC_ATOP)
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
            netEarningsChart.legend.textColor = getCompatColor(R.color.md_white_1000)
            netEarningsChart.axisLeft.textColor = getCompatColor(R.color.md_white_1000)
            netEarningsChart.axisRight.textColor = getCompatColor(R.color.md_white_1000)
            netEarningsChart.xAxis.textColor = getCompatColor(R.color.md_white_1000)
            dailySummaryExtraInfoLayout.setBackgroundColor(getCompatColor(R.color.md_black_1000))
            dailySummaryChart.legend.textColor = getCompatColor(R.color.md_white_1000)
            dailySummaryChart.axisLeft.textColor = getCompatColor(R.color.md_white_1000)
            dailySummaryChart.axisRight.textColor = getCompatColor(R.color.md_white_1000)
            dailySummaryChart.xAxis.textColor = getCompatColor(R.color.md_white_1000)
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
        if(extendedFab.isVisible){
            extendedFab.isVisible = false
        }
    }

    override fun handleBack() {
        requireActivity().finish()
    }
}