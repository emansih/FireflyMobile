package xyz.hisname.fireflyiii.ui.categories

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import kotlinx.android.synthetic.main.fragment_category_detail.*
import kotlinx.android.synthetic.main.fragment_category_detail.transactionList
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.ui.transaction.TransactionAdapter
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getImprovedViewModel

class CategoryDetailsFragment: BaseDetailFragment() {

    private val categoryDetailViewModel by lazy { getImprovedViewModel(CategoryDetailViewModel::class.java) }
    private lateinit var categoryName: String
    private val categoryId by lazy { arguments?.getLong("categoryId")  ?: 0 }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_category_detail, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWidget()
    }

    private fun setWidget(){
        categoryDetailViewModel.getCategoryById(categoryId).observe(viewLifecycleOwner){ categoryData ->
            categoryName = categoryData.categoryAttributes.name
            durationText.text = getString(R.string.chart_category_in_period, categoryName,
                    DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth())
            if(categoryData.categoryAttributes.notes.isNullOrEmpty()){
                notesCard.isGone = true
            } else {
                notesText.text = categoryData.categoryAttributes.notes.toMarkDown()
            }
        }
        setBarChart()
        loadTransactionList()
    }

    private fun setBarChart(){
        val withdrawCategory = arrayListOf<BarEntry>()
        val depositCategory = arrayListOf<BarEntry>()
        zipLiveData(categoryDetailViewModel.withdrawData,
                categoryDetailViewModel.depositData).observe(viewLifecycleOwner){ categoryData ->
            categoryData.first.forEachIndexed { index, entry ->
                withdrawCategory.add(BarEntry(index.toFloat(), entry))
            }
            categoryData.second.forEachIndexed { index, entry ->
                depositCategory.add(BarEntry(index.toFloat(), entry))
            }
            val withdraw = BarDataSet(withdrawCategory, resources.getString(R.string.withdrawal))
            val deposit = BarDataSet(depositCategory, resources.getString(R.string.deposit))
            withdraw.apply {
                valueFormatter = LargeValueFormatter()
                valueTextColor = Color.RED
                color = Color.RED
                valueTextSize = 12f
            }
            deposit.apply {
                valueFormatter = LargeValueFormatter()
                valueTextColor = Color.GREEN
                color = Color.GREEN
                valueTextSize = 12f
            }
            categoryChart.apply {
                description.isEnabled = false
                /*
                 * 20 Nov 2020,  MPAndroidChart(v3.1.0)
                 * This is a hack which took me a day to conceive.
                 * In order for X Axis to correctly show the labels, there should be an empty element between each element.
                 * I have tried changing the width using barData#getGroupWidth() and groupBars(), the bar is either
                 * outside of the chart(last bar) or has a huge margin at the start(first bar)
                 * Sometimes, IndexAxisValueFormatter is crammed to only half of the chart and with a huge
                 * margin at the end if I use barData#barWidth
                 *
                 * Conclusion: The valueFormatter works for now....
                 */
                xAxis.valueFormatter = IndexAxisValueFormatter(
                        listOf(DateTimeUtil.getDayAndMonth(DateTimeUtil.getStartOfMonth()),
                                "",
                                DateTimeUtil.getDayAndMonth(DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 1)),
                                "",
                                DateTimeUtil.getDayAndMonth(DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 2)),
                                "",
                                DateTimeUtil.getDayAndMonth(DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 3)),
                                "",
                                DateTimeUtil.getDayAndMonth(DateTimeUtil.getEndOfMonth())))
                data = BarData(deposit, withdraw)
                groupBars(0f, 0.4f, 0f)
                xAxis.axisMaximum = barData.getGroupWidth(0.4f, 0f) * 5
                xAxis.setCenterAxisLabels(true)
                data.isHighlightEnabled = false
                animateY(1000)
            }
        }
        setColor()
    }


    private fun loadTransactionList(){
        val transactionAdapter = TransactionAdapter{ data -> itemClicked(data) }
        transactionList.layoutManager = LinearLayoutManager(requireContext())
        transactionList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        transactionList.adapter = transactionAdapter
        categoryDetailViewModel.getTransactionList().observe(viewLifecycleOwner){ transactionList ->
            transactionAdapter.submitData(lifecycle, transactionList)
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

    private fun setColor(){
        if(globalViewModel.isDark){
            categoryChart.legend.textColor = getCompatColor(R.color.md_white_1000)
            categoryChart.axisLeft.textColor = getCompatColor(R.color.md_white_1000)
            categoryChart.axisRight.textColor = getCompatColor(R.color.md_white_1000)
            categoryChart.xAxis.textColor = getCompatColor(R.color.md_white_1000)
        }

    }

    override fun deleteItem() {
        categoryDetailViewModel.deleteCategory().observe(viewLifecycleOwner){ isDeleted ->
            if(isDeleted){
                toastSuccess("$categoryName deleted")
                parentFragmentManager.popBackStack()
            } else {
                toastOffline("$categoryName will be deleted later")
            }
        }
    }

    override fun editItem() {
        val addCategoryFragment = AddCategoriesFragment().apply {
            arguments = bundleOf("categoryId" to categoryId)
        }
        addCategoryFragment.show(parentFragmentManager, "add_category_fragment")
    }
}