package xyz.hisname.fireflyiii.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_report.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.TransactionViewModel
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.zipLiveData
import kotlin.collections.ArrayList

class ReportFragment: Fragment() {

    private val baseUrl: String by lazy { arguments?.getString("fireflyUrl") ?: "" }
    private val accessToken: String by lazy { arguments?.getString("access_token") ?: "" }
    private val model: TransactionViewModel by lazy { getViewModel(TransactionViewModel::class.java) }
    private var depositSum = 0
    private var withdrawSum = 0
    private var dataAdapter = ArrayList<TransactionData>()
    private var month1 = 0
    private var month1With = 0
    private var month1Depot = 0
    private var month2 = 0
    private var month2With = 0
    private var month2Depot = 0
    private var month3 = 0
    private var month3With = 0
    private var month3Depot = 0
    private var month4 = 0
    private var month4With = 0
    private var month4Depot = 0
    private var month5 = 0
    private var month5With = 0
    private var month5Depot = 0
    private var month6 = 0
    private var month6With = 0
    private var month6Depot = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_report,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbar()
        getThisMonthData()
        getHistoricalData()
    }

    private fun getThisMonthData(){
        zipLiveData(model.getTransactions(baseUrl,accessToken, DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), "withdrawals"),
                model.getTransactions(baseUrl,accessToken, DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), "deposits")).observe(this, Observer {
            if(it.first.getError() == null && it.second.getError() == null){
                dataAdapter = ArrayList(it.first.getTransaction()?.data)
                if (dataAdapter.size == 0) {
                    withdrawSum = 0
                } else {
                    it.first.getTransaction()?.data?.forEachIndexed { _, element ->
                        withdrawSum += Math.abs(element.attributes.amount.toInt())
                    }
                }
                dataAdapter = ArrayList(it.second.getTransaction()?.data)
                if (dataAdapter.size == 0) {
                    depositSum = 0
                } else {
                    it.second.getTransaction()?.data?.forEachIndexed { _, element ->
                        depositSum += Math.abs(element.attributes.amount.toInt())
                    }
                }
            }
            setPieChartData()
        })
    }

    private fun setPieChartData(){
        val entries = ArrayList<PieEntry>()
        val labels = ArrayList<String>()
        labels.add("Income")
        labels.add("Expenses")
        entries.add(PieEntry(depositSum.toFloat(), "Deposit"))
        entries.add(PieEntry(withdrawSum.toFloat(), "Withdraw"))
        val dataset = PieDataSet(entries, "")
        val dataColor = ArrayList<Int>()
        for(c in ColorTemplate.MATERIAL_COLORS){
            dataColor.add(c)
        }
        dataset.apply {
            setDrawIcons(false)
            sliceSpace = 2f
            iconsOffset = MPPointF(0f, 40f)
            colors = dataColor
            valueTextSize = 15f
        }
        val data = PieData(dataset)
        val description = Description()
        description.text = "Data for " + DateTimeUtil.getCurrentMonth()
        overviewChart.apply {
            setData(data)
            setDescription(description)
            highlightValue(null)
            invalidate()
        }
    }

    private fun getHistoricalData(){
        val oneMonthAgo =
                zipLiveData(model.getTransactions(baseUrl, accessToken, DateTimeUtil.getStartOfMonth(1),
                DateTimeUtil.getEndOfMonth(1), "withdrawals"),
                model.getTransactions(baseUrl,accessToken, DateTimeUtil.getStartOfMonth(1),
                        DateTimeUtil.getEndOfMonth(1), "deposits"))
        val twoMonthAgo =
                zipLiveData(model.getTransactions(baseUrl, accessToken, DateTimeUtil.getStartOfMonth(2),
                        DateTimeUtil.getEndOfMonth(2), "withdrawals"),
                        model.getTransactions(baseUrl,accessToken, DateTimeUtil.getStartOfMonth(2),
                                DateTimeUtil.getEndOfMonth(2), "deposits"))
        val threeMonthAgo =
                zipLiveData(model.getTransactions(baseUrl, accessToken, DateTimeUtil.getStartOfMonth(3),
                DateTimeUtil.getEndOfMonth(3), "withdrawals"),
                model.getTransactions(baseUrl,accessToken, DateTimeUtil.getStartOfMonth(3),
                        DateTimeUtil.getEndOfMonth(3), "deposits"))
        val fourMonthAgo =
                zipLiveData(model.getTransactions(baseUrl, accessToken, DateTimeUtil.getStartOfMonth(4),
                        DateTimeUtil.getEndOfMonth(4), "withdrawals"),
                        model.getTransactions(baseUrl,accessToken, DateTimeUtil.getStartOfMonth(4),
                                DateTimeUtil.getEndOfMonth(4), "deposits"))
        val fiveMonthAgo =
                zipLiveData(model.getTransactions(baseUrl, accessToken, DateTimeUtil.getStartOfMonth(5),
                        DateTimeUtil.getEndOfMonth(5), "withdrawals"),
                        model.getTransactions(baseUrl,accessToken, DateTimeUtil.getStartOfMonth(5),
                                DateTimeUtil.getEndOfMonth(5), "deposits"))
        val sixMonthAgo = zipLiveData(model.getTransactions(baseUrl, accessToken, DateTimeUtil.getStartOfMonth(6),
                DateTimeUtil.getEndOfMonth(6), "withdrawals"),
                model.getTransactions(baseUrl,accessToken, DateTimeUtil.getStartOfMonth(6),
                        DateTimeUtil.getEndOfMonth(6), "deposits"))

        zipLiveData(oneMonthAgo,twoMonthAgo, threeMonthAgo, fourMonthAgo, fiveMonthAgo, sixMonthAgo).observe(this, Observer {
            // 1 month ago
            dataAdapter = ArrayList(it.first.first.getTransaction()?.data)
            if (dataAdapter.size == 0) {
                month1With = 0
            } else {
                it.first.first.getTransaction()?.data?.forEachIndexed { _, element ->
                    month1With += Math.abs(element.attributes.amount.toInt())
                }
            }
            dataAdapter = ArrayList(it.first.second.getTransaction()?.data)
            if (dataAdapter.size == 0) {
                month1Depot = 0
            } else {
                it.first.second.getTransaction()?.data?.forEachIndexed { _, element ->
                    month1Depot += Math.abs(element.attributes.amount.toInt())
                }
            }
            month1 = month1Depot - month1With
            // 2 month ago
            dataAdapter = ArrayList(it.second.first.getTransaction()?.data)
            if (dataAdapter.size == 0) {
                month2With = 0
            } else {
                it.second.first.getTransaction()?.data?.forEachIndexed { _, element ->
                    month2With += Math.abs(element.attributes.amount.toInt())
                }
            }
            dataAdapter = ArrayList(it.second.second.getTransaction()?.data)
            if (dataAdapter.size == 0) {
                month2Depot = 0
            } else {
                it.second.second.getTransaction()?.data?.forEachIndexed { _, element ->
                    month2Depot += Math.abs(element.attributes.amount.toInt())
                }
            }
            month2 = month2Depot - month2With
            // 3 month ago
            dataAdapter = ArrayList(it.third.first.getTransaction()?.data)
            if (dataAdapter.size == 0) {
                month3With = 0
            } else {
                it.third.first.getTransaction()?.data?.forEachIndexed { _, element ->
                    month3With += Math.abs(element.attributes.amount.toInt())
                }
            }
            dataAdapter = ArrayList(it.third.second.getTransaction()?.data)
            if (dataAdapter.size == 0) {
                month3Depot = 0
            } else {
                it.third.second.getTransaction()?.data?.forEachIndexed { _, element ->
                    month3Depot += Math.abs(element.attributes.amount.toInt())
                }
            }
            month3 = month3Depot - month3With
            // 4 month ago
            dataAdapter = ArrayList(it.fourth.first.getTransaction()?.data)
            if (dataAdapter.size == 0) {
                month4With = 0
            } else {
                it.fourth.first.getTransaction()?.data?.forEachIndexed { _, element ->
                    month4With += Math.abs(element.attributes.amount.toInt())
                }
            }
            dataAdapter = ArrayList(it.fourth.second.getTransaction()?.data)
            if (dataAdapter.size == 0) {
                month4Depot = 0
            } else {
                it.fourth.second.getTransaction()?.data?.forEachIndexed { _, element ->
                    month4Depot += Math.abs(element.attributes.amount.toInt())
                }
            }
            month4 = month4Depot - month4With
            // 5 month ago
            dataAdapter = ArrayList(it.fifth.first.getTransaction()?.data)
            if (dataAdapter.size == 0) {
                month5With = 0
            } else {
                it.fifth.first.getTransaction()?.data?.forEachIndexed { _, element ->
                    month5With += Math.abs(element.attributes.amount.toInt())
                }
            }
            dataAdapter = ArrayList(it.fifth.second.getTransaction()?.data)
            if (dataAdapter.size == 0) {
                month5Depot = 0
            } else {
                it.fifth.second.getTransaction()?.data?.forEachIndexed { _, element ->
                    month5Depot += Math.abs(element.attributes.amount.toInt())
                }
            }
            month5 = month5Depot - month5With
            // 6 month ago
            dataAdapter = ArrayList(it.sixth.first.getTransaction()?.data)
            if (dataAdapter.size == 0) {
                month6With = 0
            } else {
                it.sixth.first.getTransaction()?.data?.forEachIndexed { _, element ->
                    month6With += Math.abs(element.attributes.amount.toInt())
                }
            }
            dataAdapter = ArrayList(it.sixth.second.getTransaction()?.data)
            if (dataAdapter.size == 0) {
                month6Depot = 0
            } else {
                it.sixth.second.getTransaction()?.data?.forEachIndexed { _, element ->
                    month6Depot += Math.abs(element.attributes.amount.toInt())
                }
            }
            month6 = month6Depot - month6With
            setUpBarChart()
            setBalanceHistory()
        })
    }

    private fun setUpBarChart(){
        val withDrawalHistory = arrayListOf(
                BarEntry(month1With.toFloat(), month1With.toFloat()),
                        BarEntry(month2With.toFloat(), month2With.toFloat()),
                        BarEntry(month3With.toFloat(), month3With.toFloat()),
                        BarEntry(month4With.toFloat(), month4With.toFloat()),
                        BarEntry(month5With.toFloat(), month5With.toFloat()),
                        BarEntry(month6With.toFloat(), month6With.toFloat()))
        val depositHistory = arrayListOf(
                BarEntry(month1Depot.toFloat(), month1Depot.toFloat()),
                        BarEntry(month2Depot.toFloat(), month2Depot.toFloat()),
                        BarEntry(month3Depot.toFloat(), month3Depot.toFloat()),
                        BarEntry(month4Depot.toFloat(), month4Depot.toFloat()),
                        BarEntry(month5Depot.toFloat(), month5Depot.toFloat()),
                        BarEntry(month6Depot.toFloat(), month6Depot.toFloat()))
        val withDrawalSets = BarDataSet(withDrawalHistory, "Withdraws")
        val depositSets = BarDataSet(depositHistory, "Deposits")
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
        barChart.apply {
            description.isEnabled = false
            isScaleXEnabled = false
            setDrawBarShadow(false)
            setDrawGridBackground(false)
            xAxis.valueFormatter = IndexAxisValueFormatter(getMonths())
            data = BarData(depositSets, withDrawalSets)
            barData.barWidth = 0.3f
            xAxis.axisMaximum = 0 + barChart.barData.getGroupWidth(0.4f, 0f) * 6
            groupBars(0f, 0.4f, 0f)
            data.isHighlightEnabled = false
            animateY(1000)
            setTouchEnabled(true)
            invalidate()
        }
    }

    private fun setBalanceHistory(){
        val balanceHistory = arrayListOf(
                Entry(month1.toFloat(), month1.toFloat()),
                        Entry(month2.toFloat(), month2.toFloat()),
                        Entry(month3.toFloat(), month3.toFloat()),
                        Entry(month4.toFloat(), month4.toFloat()),
                        Entry(month5.toFloat(), month5.toFloat()),
                        Entry(month6.toFloat(), month6.toFloat()))
        val balanceSet = LineDataSet(balanceHistory, "Balance")
        val monthsAxis = lineChart.xAxis
        val balanceAxis = lineChart.axisRight
        balanceAxis.isEnabled = false
        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.granularity = 1f
        balanceSet.apply {
            setCircleColor(Color.MAGENTA)
            setDrawCircles(true)
            color = getColor(R.color.colorPrimaryDark)
            valueFormatter = LargeValueFormatter()
        }
        val formatter = IAxisValueFormatter { value, axis -> getMonths()[value.toInt()] }
        monthsAxis.granularity = 1f
        monthsAxis.valueFormatter = formatter
        lineChart.apply {
            description.text = "Balance last 5 months"
            xAxis.valueFormatter = IndexAxisValueFormatter(getMonths())
            animateY(1000)
            setTouchEnabled(true)
            data = LineData(balanceSet)
            invalidate()
        }
    }

    private fun getMonths(): ArrayList<String>{
        return arrayListOf(DateTimeUtil.getPreviousMonthShortName(1),
                DateTimeUtil.getPreviousMonthShortName(2),
                DateTimeUtil.getPreviousMonthShortName(3),
                DateTimeUtil.getPreviousMonthShortName(4),
                DateTimeUtil.getPreviousMonthShortName(5),
                DateTimeUtil.getPreviousMonthShortName(6))
    }

    private fun setToolbar(){
        requireActivity().activity_toolbar.isVisible = false
        overview_toolbar.title = "Report"
        overview_toolbar.navigationIcon = getDrawable(requireContext(),R.drawable.ic_arrow_left)
        overview_toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    }

    override fun onDetach(){
        super.onDetach()
        requireActivity().activity_toolbar.isVisible = true
    }
}