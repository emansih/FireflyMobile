package xyz.hisname.fireflyiii.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.view.isVisible
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_report.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.ReportPair
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.repository.viewmodel.TransactionViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import kotlin.collections.ArrayList

class ReportFragment: BaseFragment() {

    private val model by lazy { getViewModel(TransactionViewModel::class.java) }
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
    private val withdrawal by lazy { model.getTransactions(baseUrl,accessToken, DateTimeUtil.getStartOfMonth(),
            DateTimeUtil.getEndOfMonth(), "Withdrawal")}
    private val deposit by lazy { model.getTransactions(baseUrl,accessToken, DateTimeUtil.getStartOfMonth(),
            DateTimeUtil.getEndOfMonth(), "Deposit") }



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
        launch(context = Dispatchers.Main) {
            async(Dispatchers.IO) {
                withdrawal.databaseData
                deposit.databaseData
            }.await()
            val withdrawData = withdrawal.databaseData
            val depositData = deposit.databaseData
            if(withdrawal.databaseData?.size != 0 && deposit.databaseData?.size != 0){
                dataAdapter = ArrayList(withdrawData)
                if(dataAdapter.isEmpty()){
                    withdrawSum = 0
                } else {
                    withdrawData?.forEachIndexed { _, element ->
                        withdrawSum += Math.abs(element.transactionAttributes?.amount!!.toInt())
                    }
                }
                dataAdapter = ArrayList(depositData)
                if(dataAdapter.isEmpty()){
                    depositSum = 0
                } else {
                    depositData?.forEachIndexed { _, element ->
                        depositSum += Math.abs(element.transactionAttributes?.amount!!.toInt())
                    }
                }
                setPieChartData()
            } else {
                overviewChart.setNoDataText("No data found")
            }
        }
    }

    private fun setPieChartData(){
        val dataset = PieDataSet(arrayListOf(PieEntry(depositSum.toFloat(), "Deposit"),
                PieEntry(withdrawSum.toFloat(), "Withdraw")), "")
        val dataColor = ArrayList<Int>()
        for(c in ColorTemplate.MATERIAL_COLORS){ dataColor.add(c) }
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
        lateinit var oneMonthAgo: ReportPair
        lateinit var twoMonthAgo: ReportPair
        lateinit var threeMonthAgo: ReportPair
        lateinit var fourMonthAgo: ReportPair
        lateinit var fiveMonthAgo: ReportPair
        lateinit var sixMonthAgo : ReportPair
        launch(context = Dispatchers.Main) {
            async(Dispatchers.IO) {
                oneMonthAgo = getMonthData(1)
                twoMonthAgo = getMonthData(2)
                threeMonthAgo = getMonthData(3)
                fourMonthAgo = getMonthData(4)
                fiveMonthAgo = getMonthData(5)
                sixMonthAgo = getMonthData(6)
            }.await()
            dataAdapter = ArrayList(oneMonthAgo.first)
            if(dataAdapter.isNotEmpty()){
                dataAdapter.forEachIndexed { _, element ->
                    month1With += Math.abs(element.transactionAttributes?.amount!!.toInt())
                }
            } else {
                month1With = 0
            }
            dataAdapter = ArrayList(oneMonthAgo.second)
            if(dataAdapter.isNotEmpty()){
                dataAdapter.forEachIndexed { _, element ->
                    month1Depot += Math.abs(element.transactionAttributes?.amount!!.toInt())
                }
            } else {
                month1Depot = 0
            }
            month1 = month1Depot - month1With

            dataAdapter = ArrayList(twoMonthAgo.first)
            if(dataAdapter.isNotEmpty()){
                dataAdapter.forEachIndexed { _, element ->
                    month2With += Math.abs(element.transactionAttributes?.amount!!.toInt())
                }
            } else {
                month2With = 0
            }
            dataAdapter = ArrayList(twoMonthAgo.second)
            if(dataAdapter.isNotEmpty()){
                dataAdapter.forEachIndexed { _, element ->
                    month2Depot += Math.abs(element.transactionAttributes?.amount!!.toInt())
                }
            } else {
                month2Depot = 0
            }
            month2 = month2Depot - month2With

            dataAdapter = ArrayList(threeMonthAgo.first)
            if(dataAdapter.isNotEmpty()){
                dataAdapter.forEachIndexed { _, element ->
                    month3With += Math.abs(element.transactionAttributes?.amount!!.toInt())
                }
            } else {
                month3With = 0
            }
            dataAdapter = ArrayList(threeMonthAgo.second)
            if(dataAdapter.isNotEmpty()){
                dataAdapter.forEachIndexed { _, element ->
                    month3Depot += Math.abs(element.transactionAttributes?.amount!!.toInt())
                }
            } else {
                month3Depot = 0
            }
            month3 = month3Depot - month3With

            dataAdapter = ArrayList(fourMonthAgo.first)
            if(dataAdapter.isNotEmpty()){
                dataAdapter.forEachIndexed { _, element ->
                    month4With += Math.abs(element.transactionAttributes?.amount!!.toInt())
                }
            } else {
                month4With = 0
            }
            dataAdapter = ArrayList(fourMonthAgo.second)
            if(dataAdapter.isNotEmpty()){
                dataAdapter.forEachIndexed { _, element ->
                    month4Depot += Math.abs(element.transactionAttributes?.amount!!.toInt())
                }
            } else {
                month4Depot = 0
            }
            month4 = month4Depot - month4With

            dataAdapter = ArrayList(fiveMonthAgo.first)
            if(dataAdapter.isNotEmpty()){
                dataAdapter.forEachIndexed { _, element ->
                    month5With += Math.abs(element.transactionAttributes?.amount!!.toInt())
                }
            } else {
                month5With = 0
            }
            dataAdapter = ArrayList(fiveMonthAgo.second)
            if(dataAdapter.isNotEmpty()){
                dataAdapter.forEachIndexed { _, element ->
                    month5Depot += Math.abs(element.transactionAttributes?.amount!!.toInt())
                }
            } else {
                month5Depot = 0
            }
            month5 = month5Depot - month5With

            dataAdapter = ArrayList(sixMonthAgo.first)
            if(dataAdapter.isNotEmpty()){
                dataAdapter.forEachIndexed { _, element ->
                    month6With += Math.abs(element.transactionAttributes?.amount!!.toInt())
                }
            } else {
                month6With = 0
            }
            dataAdapter = ArrayList(sixMonthAgo.second)
            if(dataAdapter.isNotEmpty()){
                dataAdapter.forEachIndexed { _, element ->
                    month6Depot += Math.abs(element.transactionAttributes?.amount!!.toInt())
                }
            } else {
                month6Depot = 0
            }
            month6 = month6Depot - month6With
            setUpBarChart()
            setBalanceHistory()
        }
    }

    private fun getMonthData(duration: Long): ReportPair{
        return ReportPair(model.getTransactions(baseUrl, accessToken, DateTimeUtil.getStartOfMonth(duration),
                DateTimeUtil.getEndOfMonth(duration), "Withdrawal").databaseData,
                model.getTransactions(baseUrl,accessToken, DateTimeUtil.getStartOfMonth(duration),
                        DateTimeUtil.getEndOfMonth(duration), "Deposit").databaseData)
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
                Entry(1f,month1.toFloat()),
                Entry(2f, month2.toFloat()),
                Entry(3f,month3.toFloat()),
                Entry(4f,month4.toFloat()),
                Entry(5f,month5.toFloat()),
                Entry(6f, month6.toFloat()))
        val balanceSet = LineDataSet(balanceHistory, "Balance")
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
        val formatter = IAxisValueFormatter { value, axis ->
            if(value >= 0 ) {
                if (value <= getMonths().size - 1) {
                    return@IAxisValueFormatter getMonths()[value.toInt()]
                }
            }
            ""
        }

        lineChart.apply {
            description.text = "Balance last 5 months"
            xAxis.granularity = 1f
            xAxis.setCenterAxisLabels(true)
            xAxis.valueFormatter = formatter
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