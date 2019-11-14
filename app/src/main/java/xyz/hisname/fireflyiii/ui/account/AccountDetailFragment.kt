package xyz.hisname.fireflyiii.ui.account

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.fragment_account_detail.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.LocaleNumberParser
import xyz.hisname.fireflyiii.util.extension.*
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
        accountViewModel.getAccountById(accountId).observe(this){ accountData ->
            val currencyCode = accountData[0].accountAttributes?.currency_code ?: ""
            val currencySymbol = accountData[0].accountAttributes?.currency_symbol ?: ""
            val accountName = accountData[0].accountAttributes?.name ?: ""
            val accountBalance = accountData[0].accountAttributes?.current_balance ?: 0.0
            currencyViewModel.getCurrencyByCode(currencyCode).observe(this){
                setLineChart(currencyCode, accountName, accountBalance)
                getAccountTransaction(accountName)
                setExpensesByCategory(currencyCode, accountName, currencySymbol)
            }
        }
        setDarkMode()
    }

    private fun setLineChart(currencyCode: String, accountName: String, accountBalance: Double){
        zipLiveData(transactionViewModel.getTransactionsByAccountAndCurrencyCodeAndDate(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 1),
                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 1),
                currencyCode, accountName),
                transactionViewModel.getTransactionsByAccountAndCurrencyCodeAndDate(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 2),
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 2),
                        currencyCode, accountName),
                transactionViewModel.getTransactionsByAccountAndCurrencyCodeAndDate(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 3),
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 3),
                        currencyCode, accountName),
                transactionViewModel.getTransactionsByAccountAndCurrencyCodeAndDate(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 4),
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 4),
                        currencyCode, accountName),
                transactionViewModel.getTransactionsByAccountAndCurrencyCodeAndDate(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 5),
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 5),
                        currencyCode, accountName),
                transactionViewModel.getTransactionsByAccountAndCurrencyCodeAndDate(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                        DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                        currencyCode, accountName)).observe(this) { transactionData ->
            val firstEntry = accountBalance.toBigDecimal().plus(transactionData.first)
            val secondEntry = firstEntry.plus(transactionData.second)
            val thirdEntry = secondEntry.plus(transactionData.third)
            val fourthEntry = thirdEntry.plus(transactionData.fourth)
            val fifthEntry = fourthEntry.plus(transactionData.fifth)
            val sixthEntry = fifthEntry.plus(transactionData.sixth)
            val lineChartEntries = arrayListOf(
                    Entry(0f, firstEntry.toFloat()),
                    Entry(1f, secondEntry.toFloat()),
                    Entry(2f, thirdEntry.toFloat()),
                    Entry(3f, fourthEntry.toFloat()),
                    Entry(4f, fifthEntry.toFloat()),
                    Entry(5f, sixthEntry.toFloat())
            )
            val dataSet = LineDataSet(lineChartEntries, accountName)
            dataSet.apply {
                setCircleColor(getCompatColor(R.color.primary))
                valueTextColor = Color.BLUE
                valueTextSize = 15f
            }
            val lineChartData = LineData(dataSet)
            transactionLineChart.apply {
                xAxis.granularity = 1f
                xAxis.valueFormatter = IndexAxisValueFormatter(arrayListOf(
                        DateTimeUtil.getDayAndMonth(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 1)),
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
                animateY(1000, Easing.EaseOutBack)
            }
        }
        balanceHistoryCardText.text = resources.getString(R.string.account_chart_description,
                accountName, DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                DateTimeUtil.getTodayDate())
    }

    private fun setExpensesByCategory(currencyCode: String, accountName: String, currencySymbol: String){
        zipLiveData(transactionViewModel.getTotalTransactionAmountByDateAndCurrency(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                DateTimeUtil.getTodayDate(), currencyCode, accountName, "Withdrawal"),
                transactionViewModel.getUniqueCategoryByDate(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                        DateTimeUtil.getTodayDate(), currencyCode, accountName, "Withdrawal")).observe(this){ transactionData ->
            setExpensesByBudget(currencyCode, accountName, currencySymbol, transactionData.first)
            pieEntryArray = ArrayList(transactionData.second.size)
            incomePieEntryArray = ArrayList(transactionData.second.size)
            transactionData.second.forEachIndexed { _, uniqueMeow ->
                setIncomeByCategory(currencyCode, accountName, uniqueMeow, transactionData.first)
                transactionViewModel.getTransactionByDateAndCategoryAndCurrency(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                        DateTimeUtil.getTodayDate(), currencyCode, accountName,
                        "Withdrawal", uniqueMeow).observe(this){ transactionAmount ->
                    val percentageCategory: Double = transactionAmount.absoluteValue.roundToInt().toDouble().div(transactionData.first.absoluteValue.roundToInt().toDouble()).times(100)
                    if (uniqueMeow == "null" || uniqueMeow == null) {
                        pieEntryArray.add(PieEntry(percentageCategory.roundToInt().toFloat(), "No Category", transactionAmount))
                    } else {
                        pieEntryArray.add(PieEntry(percentageCategory.roundToInt().toFloat(), uniqueMeow, transactionAmount))
                    }
                    pieDataSet = PieDataSet(pieEntryArray, "")
                    pieDataSet.valueFormatter = PercentFormatter()
                    pieDataSet.colors = coloring
                    pieDataSet.valueTextSize = 15f
                    categoryPieChart.data = PieData(pieDataSet)
                    categoryPieChart.invalidate()
                }
            }
            categoryPieChart.setData {
                description.text = currencySymbol + LocaleNumberParser.parseDecimal(transactionData.first, requireContext())
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener{
                    override fun onNothingSelected() {
                    }
                    override fun onValueSelected(e: Entry, h: Highlight) {
                        toastInfo(currencySymbol + e.data)
                    }
                })
            }
        }
    }

    private fun setExpensesByBudget(currencyCode: String, accountName: String, currencySymbol: String, totalAmount: Double){
        transactionViewModel.getUniqueBudgetByDate(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                DateTimeUtil.getTodayDate(), currencyCode, accountName, "Withdrawal").observe(this) { transactionData ->
            if(transactionData.isNotEmpty()){
                pieEntryBudgetArray = ArrayList(transactionData.size)
                transactionData.forEachIndexed { _, uniqueBudget ->
                    transactionViewModel.getTransactionByDateAndBudgetAndCurrency(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                            DateTimeUtil.getTodayDate(), currencyCode, accountName,
                            "Withdrawal", uniqueBudget).observe(this) { transactionAmount ->
                        val percentageCategory: Double = transactionAmount.absoluteValue.roundToInt().toDouble().div(totalAmount.absoluteValue.roundToInt().toDouble()).times(100)
                        if (uniqueBudget == "null" || uniqueBudget == null) {
                            pieEntryBudgetArray.add(PieEntry(percentageCategory.roundToInt().toFloat(), "No Budget", transactionAmount))
                        } else {
                            pieEntryBudgetArray.add(PieEntry(percentageCategory.roundToInt().toFloat(), uniqueBudget, transactionAmount))
                        }
                        pieDataSetBudget = PieDataSet(pieEntryBudgetArray, "")
                        pieDataSetBudget.valueFormatter = PercentFormatter()
                        pieDataSetBudget.colors = coloring
                        pieDataSetBudget.valueTextSize = 15f
                        budgetPieChart.data = PieData(pieDataSetBudget)
                        budgetPieChart.invalidate()
                    }
                }
                budgetPieChart.setData {
                    description.text = currencySymbol + LocaleNumberParser.parseDecimal(totalAmount, requireContext())
                    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                        override fun onNothingSelected() {
                        }

                        override fun onValueSelected(e: Entry, h: Highlight) {
                            toastInfo(currencySymbol + e.data)
                        }
                    })
                }
            } else {
                budgetPieChart.invalidate()
            }
        }
    }

    private fun setDarkMode(){
        if(isDarkMode()){
            transactionLineChart.xAxis.textColor = getCompatColor(R.color.white)
            transactionLineChart.legend.textColor =getCompatColor(R.color.white)
            transactionLineChart.axisLeft.textColor = getCompatColor(R.color.white)
        }
    }

    private fun setIncomeByCategory(currencyCode: String, accountName: String, categoryName: String,
                                    totalSum: Double){
        transactionViewModel.getTransactionByDateAndCategoryAndCurrency(DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                DateTimeUtil.getTodayDate(), currencyCode, accountName,
                "Deposit", categoryName).observe(this) { transactionAmount ->
            val percentageCategory: Double = transactionAmount.absoluteValue.roundToInt().toDouble().div(totalSum.absoluteValue.roundToInt().toDouble()).times(100)
            if (categoryName == "null" || categoryName == null) {
                incomePieEntryArray.add(PieEntry(percentageCategory.roundToInt().toFloat(), "No Category", transactionAmount))
            } else {
                incomePieEntryArray.add(PieEntry(percentageCategory.roundToInt().toFloat(), categoryName, transactionAmount))
            }
            incomePieDataSet = PieDataSet(incomePieEntryArray, "")
            incomePieDataSet.valueFormatter = PercentFormatter()
            incomePieDataSet.colors = coloring
            incomePieDataSet.valueTextSize = 15f
            incomePieChart.setData {
                data = PieData(incomePieDataSet)
            }
        }
    }

    private fun getAccountTransaction(accountName: String){
        accountTransactionList.layoutManager = LinearLayoutManager(requireContext())
        accountTransactionList.addItemDecoration(DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL))
        transactionViewModel.getTransactionListByDateAndAccount(DateTimeUtil.getDaysBefore(
                DateTimeUtil.getTodayDate(), 6), DateTimeUtil.getTodayDate(), accountName).observe(this){ transactionData ->
          // TODO: FIX ME
            //  val rtAdapter = TransactionRecyclerAdapter(transactionData){ data -> itemClicked(data) }
           // accountTransactionList.adapter = rtAdapter
           // rtAdapter.apply { accountTransactionList.adapter as TransactionRecyclerAdapter }
           // rtAdapter.notifyDataSetChanged()
        }
    }

    private fun itemClicked(data: Transactions){
        requireFragmentManager().commit {
            replace(R.id.fragment_container, TransactionDetailsFragment().apply {
                arguments = bundleOf("transactionId" to data.transaction_journal_id)
            })
            addToBackStack(null)
        }
    }

    override fun deleteItem() {
        accountViewModel.isLoading.observe(this){
            if(it == true){
                ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            }
        }
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_account_title, accountNameString))
                .setMessage(resources.getString(R.string.delete_account_message, accountNameString))
                .setPositiveButton(R.string.delete_permanently) { _, _ ->
                    accountViewModel.deleteAccountById(accountId).observe(this) {
                        if(it == true){
                            requireFragmentManager().popBackStack()
                            toastSuccess("Account Deleted")
                        } else {
                            toastError("Account will be deleted later")
                        }
                    }
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
            requireFragmentManager().commit {
                replace(R.id.bigger_fragment_container, AddAccountFragment().apply{
                    arguments = bundleOf("accountType" to accountType, "accountId" to accountId)
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