package xyz.hisname.fireflyiii.ui.account

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.fragment_account_detail.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.ui.transaction.TransactionRecyclerAdapter
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.MpAndroidPercentFormatter
import xyz.hisname.fireflyiii.util.extension.*
import java.text.DecimalFormat
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class AccountDetailFragment: BaseDetailFragment() {

    private val accountId: Long by lazy { arguments?.getLong("accountId") as Long  }
    private var accountNameString: String = ""
    private var pieEntryArray = arrayListOf<PieEntry>()
    private var pieEntryBudgetArray = arrayListOf<PieEntry>()
    private var pieDataSet: PieDataSet = PieDataSet(pieEntryArray, "")
    private var pieDataSetBudget: PieDataSet = PieDataSet(pieEntryBudgetArray, "")
    private var incomePieEntryArray = arrayListOf<PieEntry>()
    private var incomePieDataSet = PieDataSet(incomePieEntryArray, "")
    private lateinit var accountType: String

    private val coloring = arrayListOf<Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_account_detail, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        for (col in ColorTemplate.MATERIAL_COLORS) {
            coloring.add(col)
        }
        currencyViewModel.getDefaultCurrency().observe(this, Observer { currencyData ->
            if(currencyData.isNotEmpty()){
                setLineChart(currencyData[0].currencyAttributes?.code ?: "", currencyData[0].currencyAttributes?.symbol ?: "")
            }
        })
        setDarkMode()
    }

    private fun setLineChart(currencyCode: String, currencySymbol: String){
        accountViewModel.getAccountById(accountId).observe(this, Observer { accountData ->
            accountType = accountData[0].accountAttributes?.type ?: ""
            if(accountData.isNotEmpty()){
                zipLiveData(transactionViewModel.getTransactionsByAccountAndCurrencyCodeAndDate(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 1),
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 1),
                        currencyCode, accountData[0].accountAttributes!!.name),
                        transactionViewModel.getTransactionsByAccountAndCurrencyCodeAndDate(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 2),
                                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 2),
                                currencyCode, accountData[0].accountAttributes!!.name),
                        transactionViewModel.getTransactionsByAccountAndCurrencyCodeAndDate(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 3),
                                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 3),
                                currencyCode, accountData[0].accountAttributes!!.name),
                        transactionViewModel.getTransactionsByAccountAndCurrencyCodeAndDate(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 4),
                                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 4),
                                currencyCode, accountData[0].accountAttributes!!.name),
                        transactionViewModel.getTransactionsByAccountAndCurrencyCodeAndDate(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 5),
                                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 5),
                                currencyCode, accountData[0].accountAttributes!!.name),
                        transactionViewModel.getTransactionsByAccountAndCurrencyCodeAndDate(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                                currencyCode, accountData[0].accountAttributes!!.name)).observe(this, Observer { transactionData ->
                    transactionViewModel.isLoading.observe(this, Observer { loader ->
                        if(!loader){
                            val firstEntry = accountData[0].accountAttributes?.current_balance?.toBigDecimal()?.plus(transactionData.first)
                            val secondEntry = firstEntry?.plus(transactionData.second)
                            val thirdEntry = secondEntry?.plus(transactionData.third)
                            val fourthEntry = thirdEntry?.plus(transactionData.fourth)
                            val fifthEntry = fourthEntry?.plus(transactionData.fifth)
                            val sixthEntry = fifthEntry?.plus(transactionData.sixth)
                            val lineChartEntries = arrayListOf(
                                    Entry(0f,firstEntry?.toFloat() ?: 0f),
                                    Entry(1f,secondEntry?.toFloat() ?: 0f),
                                    Entry(2f,thirdEntry?.toFloat() ?: 0f),
                                    Entry(3f,fourthEntry?.toFloat() ?: 0f),
                                    Entry(4f,fifthEntry?.toFloat() ?: 0f),
                                    Entry(5f,sixthEntry?.toFloat() ?: 0f)
                            )
                            val dataSet = LineDataSet(lineChartEntries, accountData[0].accountAttributes?.name)
                            dataSet.apply {
                                setCircleColor(ContextCompat.getColor(requireContext(), R.color.primary))
                                valueTextColor = Color.BLUE
                                valueTextSize = 15f
                            }
                            val lineChartData = LineData(dataSet)
                            if(isDarkMode()){
                                transactionLineChart.xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.white)
                                transactionLineChart.legend.textColor = ContextCompat.getColor(requireContext(), R.color.white)
                                transactionLineChart.axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.white)
                            }
                            transactionLineChart.apply {
                                xAxis.granularity = 1f
                                xAxis.valueFormatter = IndexAxisValueFormatter(arrayListOf(
                                        DateTimeUtil.getDayAndMonth(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(),1)),
                                        DateTimeUtil.getDayAndMonth(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 2)),
                                        DateTimeUtil.getDayAndMonth(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 3)),
                                        DateTimeUtil.getDayAndMonth(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 4)),
                                        DateTimeUtil.getDayAndMonth(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 5)),
                                        DateTimeUtil.getDayAndMonth(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6))))
                                xAxis.setDrawLabels(true)
                                xAxis.setDrawAxisLine(false)
                                xAxis.setDrawGridLines(false)
                                data = lineChartData
                                axisRight.isEnabled = false
                                description.isEnabled = false
                                xAxis.position = XAxis.XAxisPosition.BOTTOM
                                data.isHighlightEnabled = false
                                setTouchEnabled(true)
                                setPinchZoom(true)
                                animateY(1000, Easing.EasingOption.EaseOutBack)
                            }
                        }
                    })
                })
                balanceHistoryCardText.text = "Chart for all transactions for account " +
                        accountData[0].accountAttributes?.name + " between " +
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6) +
                        " and " + DateTimeUtil.getTodayDate()
                getAccountTransaction(accountData[0].accountAttributes?.name ?: "")
                setExpensesByCategory(currencyCode, accountData[0].accountAttributes?.name ?: "", currencySymbol)

            }
        })
    }

    private fun setExpensesByCategory(currencyCode: String, accountName: String, currencySymbol: String){
        zipLiveData(transactionViewModel.getTotalTransactionAmountByDateAndCurrency(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                DateTimeUtil.getTodayDate(), currencyCode, accountName, "Withdrawal"),transactionViewModel.getUniqueCategoryByDate(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                DateTimeUtil.getTodayDate(), currencyCode, accountName, "Withdrawal")).observe(this, Observer { transactionData ->
            if(transactionData.second.isNotEmpty()) {
                setExpensesByBudget(currencyCode, accountName, currencySymbol, transactionData.first)
                pieEntryArray = ArrayList(transactionData.second.size)
                incomePieEntryArray = ArrayList(transactionData.second.size)
                transactionData.second.forEachIndexed { _, uniqueMeow ->
                    setIncomeByCategory(currencyCode, accountName, uniqueMeow, transactionData.first)
                    transactionViewModel.getTransactionByDateAndCategoryAndCurrency(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                            DateTimeUtil.getTodayDate(), currencyCode, accountName,
                            "Withdrawal", uniqueMeow).observe(this, Observer { transactionAmount ->
                        val percentageCategory: Double = transactionAmount.absoluteValue.roundToInt().toDouble().div(transactionData.first.absoluteValue.roundToInt().toDouble()).times(100)
                        if (uniqueMeow == "null" || uniqueMeow == null) {
                            pieEntryArray.add(PieEntry(percentageCategory.roundToInt().toFloat(), "No Category", transactionAmount))
                        } else {
                            pieEntryArray.add(PieEntry(percentageCategory.roundToInt().toFloat(), uniqueMeow, transactionAmount))
                        }
                        pieDataSet = PieDataSet(pieEntryArray, "")
                        pieDataSet.valueFormatter = MpAndroidPercentFormatter()
                        pieDataSet.colors = coloring
                        pieDataSet.valueTextSize = 15f
                        categoryPieChart.data = PieData(pieDataSet)
                        categoryPieChart.invalidate()
                    })
                }
                if(isDarkMode()){
                    categoryPieChart.legend.textColor = ContextCompat.getColor(requireContext(), R.color.white)
                    categoryPieChart.description.textColor = ContextCompat.getColor(requireContext(), R.color.white)
                }
                val decimalFormat = DecimalFormat(".##")
                categoryPieChart.apply {
                    description.text = currencySymbol + decimalFormat.format(transactionData.first)
                    description.textSize = 15f
                    legend.form = Legend.LegendForm.CIRCLE
                    isDrawHoleEnabled = false
                    setUsePercentValues(true)
                    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                    setTransparentCircleAlpha(0)
                }
                categoryPieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener{
                    override fun onNothingSelected() {
                    }

                    override fun onValueSelected(e: Entry, h: Highlight) {
                       toastInfo(currencySymbol + e.data)
                    }

                })
            } else {
                categoryPieChart.apply {
                    description.text = "No expenses Found!"
                    description.textSize = 15f
                    legend.form = Legend.LegendForm.CIRCLE
                    isDrawHoleEnabled = false
                    setUsePercentValues(true)
                    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                    setTransparentCircleAlpha(0)
                }
                incomePieChart.apply {
                    description.text = "No deposits Found!"
                    description.textSize = 15f
                    legend.form = Legend.LegendForm.CIRCLE
                    isDrawHoleEnabled = false
                    setUsePercentValues(true)
                    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                    setTransparentCircleAlpha(0)
                }
            }
        })
    }

    private fun setExpensesByBudget(currencyCode: String, accountName: String, currencySymbol: String, totalAmount: Double){
        transactionViewModel.getUniqueBudgetByDate(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                DateTimeUtil.getTodayDate(), currencyCode, accountName, "Withdrawal").observe(this, Observer { transactionData ->
            if(transactionData.isNotEmpty()){
                pieEntryBudgetArray = ArrayList(transactionData.size)
                transactionData.forEachIndexed { _, uniqueBudget ->
                    transactionViewModel.getTransactionByDateAndBudgetAndCurrency(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                            DateTimeUtil.getTodayDate(), currencyCode, accountName,
                            "Withdrawal", uniqueBudget).observe(this, Observer { transactionAmount ->
                        val percentageCategory: Double = transactionAmount.absoluteValue.roundToInt().toDouble().div(totalAmount.absoluteValue.roundToInt().toDouble()).times(100)
                        if (uniqueBudget == "null" || uniqueBudget == null) {
                            pieEntryBudgetArray.add(PieEntry(percentageCategory.roundToInt().toFloat(), "No Budget", transactionAmount))
                        } else {
                            pieEntryBudgetArray.add(PieEntry(percentageCategory.roundToInt().toFloat(), uniqueBudget, transactionAmount))
                        }
                        pieDataSetBudget = PieDataSet(pieEntryBudgetArray, "")
                        pieDataSetBudget.valueFormatter = MpAndroidPercentFormatter()
                        pieDataSetBudget.colors = coloring
                        pieDataSetBudget.valueTextSize = 15f
                        budgetPieChart.data = PieData(pieDataSetBudget)
                        budgetPieChart.invalidate()
                    })
                }
                val decimalFormat = DecimalFormat(".##")
                budgetPieChart.apply {
                    description.text = currencySymbol + decimalFormat.format(totalAmount)
                    description.textSize = 15f
                    legend.form = Legend.LegendForm.CIRCLE
                    isDrawHoleEnabled = false
                    setUsePercentValues(true)
                    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                    setTransparentCircleAlpha(0)
                }
                budgetPieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener{
                    override fun onNothingSelected() {
                    }

                    override fun onValueSelected(e: Entry, h: Highlight) {
                        toastInfo(currencySymbol + e.data)
                    }

                })
            } else {
                budgetPieChart.invalidate()
            }
        })
    }

    private fun setDarkMode(){
        if(isDarkMode()){
            budgetPieChart.legend.textColor = ContextCompat.getColor(requireContext(), R.color.white)
            budgetPieChart.description.textColor = ContextCompat.getColor(requireContext(), R.color.white)
            incomePieChart.legend.textColor = ContextCompat.getColor(requireContext(), R.color.white)
            incomePieChart.description.textColor = ContextCompat.getColor(requireContext(), R.color.white)
        }
    }

    private fun setIncomeByCategory(currencyCode: String, accountName: String, categoryName: String,
                                    totalSum: Double){
        transactionViewModel.getTransactionByDateAndCategoryAndCurrency(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                DateTimeUtil.getTodayDate(), currencyCode, accountName,
                "Deposit", categoryName).observe(this, Observer { transactionAmount ->
            val percentageCategory: Double = transactionAmount.absoluteValue.roundToInt().toDouble().div(totalSum.absoluteValue.roundToInt().toDouble()).times(100)
            if (categoryName == "null" || categoryName == null) {
                incomePieEntryArray.add(PieEntry(percentageCategory.roundToInt().toFloat(), "No Category", transactionAmount))
            } else {
                incomePieEntryArray.add(PieEntry(percentageCategory.roundToInt().toFloat(), categoryName, transactionAmount))
            }
            incomePieDataSet = PieDataSet(incomePieEntryArray, "")
            incomePieDataSet.valueFormatter = MpAndroidPercentFormatter()
            incomePieDataSet.colors = coloring
            incomePieDataSet.valueTextSize = 15f
            incomePieChart.data = PieData(incomePieDataSet)
            incomePieChart.invalidate()
        })
    }

    private fun getAccountTransaction(accountName: String){
        accountTransactionList.layoutManager = LinearLayoutManager(requireContext())
        accountTransactionList.addItemDecoration(DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL))
        transactionViewModel.getTransactionListByDateAndAccount(DateTimeUtil.getDaysBefore(
                DateTimeUtil.getTodayDate(), 6), DateTimeUtil.getTodayDate(), accountName).observe(this,
                Observer { transactionData ->
                    val rtAdapter = TransactionRecyclerAdapter(transactionData){ data: TransactionData -> itemClicked(data) }
                    accountTransactionList.adapter = rtAdapter
                    rtAdapter.apply { accountTransactionList.adapter as TransactionRecyclerAdapter }
                    rtAdapter.notifyDataSetChanged()
        })
    }

    private fun itemClicked(data: TransactionData){
        requireFragmentManager().commit {
            replace(R.id.fragment_container, TransactionDetailsFragment().apply {
                arguments = bundleOf("transactionId" to data.transactionId)
            })
            addToBackStack(null)
        }
    }

    override fun deleteItem() {
        accountViewModel.isLoading.observe(this, Observer {
            if(it == true){
                ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            }
        })
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_account_title, accountNameString))
                .setMessage(resources.getString(R.string.delete_account_message, accountNameString))
                .setPositiveButton(R.string.delete_permanently) { _, _ ->
                    accountViewModel.deleteAccountById(accountId).observe(this, Observer {
                        if(it == true){
                            requireFragmentManager().popBackStack()
                            toastSuccess("Account Deleted")
                        } else {
                            toastError("Account will be deleted later")
                        }
                    })
                }
                .setNegativeButton(android.R.string.no){dialog, _ ->
                    dialog.dismiss()
                }
                .show()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId){
        R.id.menu_item_delete -> consume {
            deleteItem()
        }
        android.R.id.home -> consume {
            requireFragmentManager().popBackStack()
        }
        R.id.menu_item_edit -> consume {
            val account = bundleOf("accountId" to accountId)
            requireFragmentManager().commit {
                replace(R.id.bigger_fragment_container, AddAccountFragment().apply{
                    arguments = bundleOf("accountType" to accountType, "accountId" to account)
                })
                addToBackStack(null)

            }
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun handleBack() {
        requireFragmentManager().popBackStack()
    }
}