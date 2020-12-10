package xyz.hisname.fireflyiii.ui.dashboard

import android.animation.ObjectAnimator
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
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.budget.BudgetSummaryFragment
import xyz.hisname.fireflyiii.ui.transaction.TransactionAdapter
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionActivity
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.ui.widgets.BalanceWidget
import xyz.hisname.fireflyiii.ui.widgets.BillsToPayWidget
import xyz.hisname.fireflyiii.util.*
import xyz.hisname.fireflyiii.util.extension.*
import java.math.BigDecimal


class DashboardFragment: BaseFragment() {

    private val transactionExtendedFab by bindView<ExtendedFloatingActionButton>(R.id.addTransactionExtended)
    private val dashboardView by lazy { getImprovedViewModel(DashboardViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_dashboard,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dashboardView.getDefaultCurrency()
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
        setSummary()
        dashboardView.currencySymbol.observe(viewLifecycleOwner){ symbol ->
            setPieChart()
            setNetIncome(symbol)
            setAverage()
            loadRecentTransaction()
        }
        setExtendedFab()
        budgetCard.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, BudgetSummaryFragment())
                addToBackStack(null)
            }
        }
        setIcon()
        dashboardView.apiResponse.observe(viewLifecycleOwner){ response ->
            Snackbar.make(coordinatorlayout, response, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setExtendedFab(){
        transactionExtendedFab.isVisible = true
        dashboardNested.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
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

    private fun setSummary(){
        dashboardView.networthValue.observe(viewLifecycleOwner){ money ->
             networthAmount.text = money
        }

        dashboardView.leftToSpendValue.observe(viewLifecycleOwner){ money ->
            leftToSpendAmountText.text = money
        }
        dashboardView.balanceValue.observe(viewLifecycleOwner){ money ->
            balanceText.text = money
            updateHomeScreenWidget(BalanceWidget::class.java)
        }
        dashboardView.earnedValue.observe(viewLifecycleOwner){ money ->
            balanceEarnedText.text = money + " + "
        }
        dashboardView.spentValue.observe(viewLifecycleOwner){ money ->
            balanceSpentText.text = money
        }
        dashboardView.billsToPay.observe(viewLifecycleOwner){ money ->
            billsText.text = money
            updateHomeScreenWidget(BillsToPayWidget::class.java)
        }

        dashboardView.billsPaid.observe(viewLifecycleOwner){ money ->
            billsPaidText.text = money
        }

        dashboardView.leftToSpendDay.observe(viewLifecycleOwner){ money ->
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

    private fun setNetIncome(currencySymbol: String){
        zipLiveData(dashboardView.currentMonthDepositLiveData, dashboardView.currentMonthWithdrawalLiveData,
                dashboardView.lastMonthDepositLiveData, dashboardView.lastMonthWithdrawalLiveData,
                dashboardView.twoMonthsAgoDepositLiveData,
                dashboardView.twoMonthsAgoWithdrawalLiveData).observe(viewLifecycleOwner){ value ->
            currentMonthIncome.text = dashboardView.currentMonthDeposit
            currentExpense.text = dashboardView.currentMonthWithdrawal
            if(dashboardView.currentMonthNetBigDecimal < BigDecimal.ZERO){
                currentNetIncome.setTextColor(getCompatColor(R.color.md_red_700))
            }
            currentNetIncome.text = dashboardView.currentMonthNetString

            oneMonthBeforeIncome.text = dashboardView.lastMonthDeposit
            oneMonthBeforeExpense.text = dashboardView.lastMonthWithdrawal

            if(dashboardView.lastMonthNetBigDecimal < BigDecimal.ZERO){
                oneMonthBeforeNetIncome.setTextColor(getCompatColor(R.color.md_red_700))
            }
            oneMonthBeforeNetIncome.text = dashboardView.lastMonthNetString


            twoMonthBeforeIncome.text = dashboardView.twoMonthsAgoDeposit
            twoMonthBeforeExpense.text = dashboardView.twoMonthsAgoWithdrawal
            if(dashboardView.twoMonthAgoNetBigDecimal < BigDecimal.ZERO){
                twoMonthBeforeNetIncome.setTextColor(getCompatColor(R.color.md_red_700))
            }

            twoMonthBeforeNetIncome.text = dashboardView.twoMonthAgoNetString

            val withDrawalHistory = arrayListOf(
                    BarEntry(0f, value.sixth.toFloat()),
                    BarEntry(1f, value.fourth.toFloat()),
                    BarEntry(2f, value.second.toFloat()))
            val depositHistory = arrayListOf(
                    BarEntry(0f, value.fifth.toFloat()),
                    BarEntry(1f, value.third.toFloat()),
                    BarEntry(2f, value.fifth.toFloat()))

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
                xAxis.valueFormatter = IndexAxisValueFormatter(arrayListOf(
                        DateTimeUtil.getPreviousMonthShortName(2),
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
    }

    private fun setAverage(){
        dashboardView.sixDayWithdrawalLiveData.observe(viewLifecycleOwner){ value ->
            val expenseHistory = arrayListOf(
                    BarEntry(0f, value.first.toFloat()),
                    BarEntry(1f, value.second.toFloat()),
                    BarEntry(2f, value.third.toFloat()),
                    BarEntry(3f, value.fourth.toFloat()),
                    BarEntry(4f, value.fifth.toFloat()),
                    BarEntry(5f, value.sixth.toFloat())
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
            sixDaysAverage.text = dashboardView.sixDaysAverage
            thirtyDaysAverage.text = dashboardView.thirtyDayAverage
        }
    }

    private fun setPieChart() {
        monthText.text = DateTimeUtil.getCurrentMonth()
        val dataColor = arrayListOf(getCompatColor(R.color.md_red_700), getCompatColor(R.color.md_green_500))
        zipLiveData(dashboardView.budgetLeftPercentage,
                dashboardView.budgetSpentPercentage).observe(viewLifecycleOwner){ budget ->
            val dataSet = PieDataSet(
                    arrayListOf(PieEntry(budget.first.toFloat(), resources.getString(R.string.spent)),
                            PieEntry(budget.second.toFloat(), resources.getString(R.string.left_to_spend))), "").apply {
                setDrawIcons(true)
                sliceSpace = 2f
                iconsOffset = MPPointF(0f, 40f)
                colors = dataColor
                valueTextSize = 15f
                valueFormatter = PercentFormatter(budgetChart)
            }
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
            when {
                budget.first.toInt() >= 80 -> {
                    progressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.RED,
                            BlendModeCompat.SRC_ATOP)
                    budgetProgress.progressDrawable = progressDrawable
                }
                budget.first.toInt() in 50..80 -> {
                    progressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.YELLOW,
                            BlendModeCompat.SRC_ATOP)
                    budgetProgress.progressDrawable = progressDrawable
                }
                else -> {
                    progressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.GREEN,
                            BlendModeCompat.SRC_ATOP)
                    budgetProgress.progressDrawable = progressDrawable
                }
            }
            ObjectAnimator.ofInt(budgetProgress, "progress", budget.first.toInt()).start()
        }
        dashboardView.currentMonthBudgetValue.observe(viewLifecycleOwner){ budget ->
            budgetAmount.text = budget
        }
        dashboardView.currentMonthSpentValue.observe(viewLifecycleOwner){ budget ->
            spentAmount.text = budget
        }
    }

    private fun loadRecentTransaction(){
        val recyclerAdapter = TransactionAdapter{ data -> itemClicked(data) }
        recentTransactionList.layoutManager = LinearLayoutManager(requireContext())
        recentTransactionList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recentTransactionList.adapter = recyclerAdapter
        transactionLoader.show()
        dashboardView.getRecentTransactions().observe(viewLifecycleOwner){ pagingData ->
            recyclerAdapter.submitData(lifecycle, pagingData)
        }

        recyclerAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner){ loadStates ->
            if(loadStates.refresh !is LoadState.Loading) {
                transactionLoader.hide()
                if(recyclerAdapter.itemCount < 1) {
                    recentTransactionList.isGone = true
                    noTransactionText.isVisible = true
                } else {
                    recentTransactionList.isVisible = true
                    noTransactionText.isGone = true
                }
            }
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
                    .setDuration(2000)
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

    override fun handleBack() {
        requireActivity().finish()
    }
}