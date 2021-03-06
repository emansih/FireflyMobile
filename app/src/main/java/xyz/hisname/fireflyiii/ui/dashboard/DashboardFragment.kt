/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
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
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.FragmentDashboardBinding
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.bills.BillsBottomSheet
import xyz.hisname.fireflyiii.ui.bills.list.ListBillFragment
import xyz.hisname.fireflyiii.ui.budget.BudgetListFragment
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
    private var fragmentDashboardBinding: FragmentDashboardBinding? = null
    private val binding get() = fragmentDashboardBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentDashboardBinding = FragmentDashboardBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dashboardView.getDefaultCurrency()
        animateCard(binding.balanceCard, binding.billsCard, binding.netEarningsCard, binding.dailySummaryCard,
                binding.leftToSpendCard, binding.networthCard, binding.recentTransactionCard, binding.budgetCard)
        binding.twoMonthBefore.text = DateTimeUtil.getPreviousMonthShortName(2)
        binding.oneMonthBefore.text = DateTimeUtil.getPreviousMonthShortName(1)
        binding.currentMonthTextView.text = DateTimeUtil.getCurrentMonthShortName()
        changeTheme()
        binding.balanceCard.layoutParams.width = (getScreenWidth() - 425)
        binding.billsCard.layoutParams.width = (getScreenWidth() - 425)
        binding.leftToSpendCard.layoutParams.width = (getScreenWidth() - 425)
        binding.networthCard.layoutParams.width = (getScreenWidth() - 425)
        setSummary()
        dashboardView.currencySymbol.observe(viewLifecycleOwner){ _ ->
            setPieChart()
            setNetIncome()
            setAverage()
            loadRecentTransaction()
        }
        setExtendedFab()
        binding.budgetCard.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, BudgetSummaryFragment())
                addToBackStack(null)
            }
        }
        setIcon()
        dashboardView.apiResponse.observe(viewLifecycleOwner){ response ->
            val coordinatorLayout = requireActivity().findViewById<CoordinatorLayout>(R.id.coordinatorlayout)
            Snackbar.make(coordinatorLayout, response, Snackbar.LENGTH_LONG).show()
        }
        setDashboardDataClick()
        showCase(R.string.dashboard_balance_help_text,
                "balanceLayoutCaseView", binding.balanceLayout).show()
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = resources.getString(R.string.dashboard)
    }

    private fun setDashboardDataClick(){
        binding.leftToSpendCard.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, BudgetListFragment())
                addToBackStack(null)
            }
        }
        binding.billsCard.setOnClickListener {
            val billsBottomSheet = BillsBottomSheet()
            billsBottomSheet.show(childFragmentManager, "billsList")
        }
    }

    private fun setExtendedFab(){
        transactionExtendedFab.isVisible = true
        binding.dashboardNested.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
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
            binding.networthAmount.text = money
        }

        dashboardView.leftToSpendValue.observe(viewLifecycleOwner){ money ->
            binding.leftToSpendAmountText.text = money
        }
        dashboardView.balanceValue.observe(viewLifecycleOwner){ money ->
            binding.balanceText.text = money
            updateHomeScreenWidget(BalanceWidget::class.java)
        }
        dashboardView.earnedValue.observe(viewLifecycleOwner){ money ->
            binding.balanceEarnedText.text = money + " + "
        }
        dashboardView.spentValue.observe(viewLifecycleOwner){ money ->
            binding.balanceSpentText.text = money
        }
        dashboardView.billsToPay.observe(viewLifecycleOwner){ money ->
            binding.billsText.text = money
            updateHomeScreenWidget(BillsToPayWidget::class.java)
        }

        dashboardView.billsPaid.observe(viewLifecycleOwner){ money ->
            binding.billsPaidText.text = money
        }

        dashboardView.leftToSpendDay.observe(viewLifecycleOwner){ money ->
            binding.leftToSpendAmount.text = money
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
        binding.balanceIcon.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_balance_scale
            colorRes = R.color.md_white_1000
            sizeDp = 32
        })
        binding.billsIcon.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_calendar
            colorRes =R.color.md_white_1000
            sizeDp = 32
        })
        binding.leftToSpendIcon.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_money_bill
            colorRes = R.color.md_white_1000
            sizeDp = 32
        })
        binding.networthIcon.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_chart_line
            colorRes = R.color.md_white_1000
            sizeDp = 32
        })
    }

    private fun setNetIncome(){
        zipLiveData(dashboardView.currentMonthDepositLiveData, dashboardView.currentMonthWithdrawalLiveData,
                dashboardView.lastMonthDepositLiveData, dashboardView.lastMonthWithdrawalLiveData,
                dashboardView.twoMonthsAgoDepositLiveData,
                dashboardView.twoMonthsAgoWithdrawalLiveData).observe(viewLifecycleOwner){ value ->
            binding.currentMonthIncome.text = dashboardView.currentMonthDeposit
            binding.currentExpense.text = dashboardView.currentMonthWithdrawal
            if(dashboardView.currentMonthNetBigDecimal < BigDecimal.ZERO){
                binding.currentNetIncome.setTextColor(getCompatColor(R.color.md_red_700))
            }
            binding.currentNetIncome.text = dashboardView.currentMonthNetString

            binding.oneMonthBeforeIncome.text = dashboardView.lastMonthDeposit
            binding.oneMonthBeforeExpense.text = dashboardView.lastMonthWithdrawal

            if(dashboardView.lastMonthNetBigDecimal < BigDecimal.ZERO){
                binding.oneMonthBeforeNetIncome.setTextColor(getCompatColor(R.color.md_red_700))
            }
            binding.oneMonthBeforeNetIncome.text = dashboardView.lastMonthNetString


            binding.twoMonthBeforeIncome.text = dashboardView.twoMonthsAgoDeposit
            binding.twoMonthBeforeExpense.text = dashboardView.twoMonthsAgoWithdrawal
            if(dashboardView.twoMonthAgoNetBigDecimal < BigDecimal.ZERO){
                binding.twoMonthBeforeNetIncome.setTextColor(getCompatColor(R.color.md_red_700))
            }

            binding.twoMonthBeforeNetIncome.text = dashboardView.twoMonthAgoNetString

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
            binding.netEarningsChart.apply {
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
                xAxis.axisMaximum = binding.netEarningsChart.barData.getGroupWidth(0.4f, 0f) * 3
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
            binding.dailySummaryChart.apply {
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
            binding.sixDaysAverage.text = dashboardView.sixDaysAverage
            binding.thirtyDaysAverage.text = dashboardView.thirtyDayAverage
        }
    }

    private fun setPieChart() {
        binding.monthText.text = DateTimeUtil.getCurrentMonth()
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
                valueFormatter = PercentFormatter(binding.budgetChart)
            }
            binding.budgetChart.setData {
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
            val progressDrawable = binding.budgetProgress.progressDrawable.mutate()
            when {
                budget.first.toInt() >= 80 -> {
                    progressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.RED,
                            BlendModeCompat.SRC_ATOP)
                }
                budget.first.toInt() in 50..80 -> {
                    progressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.YELLOW,
                            BlendModeCompat.SRC_ATOP)
                }
                else -> {
                    progressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.GREEN,
                            BlendModeCompat.SRC_ATOP)
                }
            }
            binding.budgetProgress.progressDrawable = progressDrawable
            ObjectAnimator.ofInt(binding.budgetProgress, "progress", budget.first.toInt()).start()
        }
        dashboardView.currentMonthBudgetValue.observe(viewLifecycleOwner){ budget ->
            binding.budgetAmount.text = budget
        }
        dashboardView.currentMonthSpentValue.observe(viewLifecycleOwner){ budget ->
            binding.spentAmount.text = budget
        }
    }

    private fun loadRecentTransaction(){
        val recyclerAdapter = TransactionAdapter{ data -> itemClicked(data) }
        binding.recentTransactionList.layoutManager = LinearLayoutManager(requireContext())
        binding.recentTransactionList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.recentTransactionList.adapter = recyclerAdapter
        binding.transactionLoader.show()
        dashboardView.getRecentTransactions().observe(viewLifecycleOwner){ pagingData ->
            recyclerAdapter.submitData(lifecycle, pagingData)
        }

        recyclerAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner){ loadStates ->
            if(loadStates.refresh !is LoadState.Loading) {
                binding.transactionLoader.hide()
                if(recyclerAdapter.itemCount < 1) {
                    binding.recentTransactionList.isGone = true
                    binding.noTransactionText.isVisible = true
                } else {
                    binding.recentTransactionList.isVisible = true
                    binding.noTransactionText.isGone = true
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
            binding.netEarningsExtraInfoLayout.setBackgroundColor(getCompatColor(R.color.md_black_1000))
            binding.netEarningsChart.legend.textColor = getCompatColor(R.color.md_white_1000)
            binding.netEarningsChart.axisLeft.textColor = getCompatColor(R.color.md_white_1000)
            binding.netEarningsChart.axisRight.textColor = getCompatColor(R.color.md_white_1000)
            binding.netEarningsChart.xAxis.textColor = getCompatColor(R.color.md_white_1000)
            binding.dailySummaryExtraInfoLayout.setBackgroundColor(getCompatColor(R.color.md_black_1000))
            binding.dailySummaryChart.legend.textColor = getCompatColor(R.color.md_white_1000)
            binding.dailySummaryChart.axisLeft.textColor = getCompatColor(R.color.md_white_1000)
            binding.dailySummaryChart.axisRight.textColor = getCompatColor(R.color.md_white_1000)
            binding.dailySummaryChart.xAxis.textColor = getCompatColor(R.color.md_white_1000)
            binding.budgetExtraInfoLayout.setBackgroundColor(getCompatColor(R.color.md_black_1000))
        }
    }

    private fun animateCard(vararg frameLayout: FrameLayout){
        for(frames in frameLayout){
            frames.translationY = getScreenHeight().toFloat()
            frames.animate()
                    .translationY(0f)
                    .setInterpolator(DecelerateInterpolator(5f))
                    .setDuration(1234)
        }
    }
}