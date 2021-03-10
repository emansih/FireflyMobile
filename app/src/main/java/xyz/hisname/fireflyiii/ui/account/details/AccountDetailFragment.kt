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

package xyz.hisname.fireflyiii.ui.account.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.DetailsCardBinding
import xyz.hisname.fireflyiii.databinding.FragmentAccountDetailBinding
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.account.AddAccountFragment
import xyz.hisname.fireflyiii.ui.base.AttachmentRecyclerAdapter
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.ui.base.BaseDetailRecyclerAdapter
import xyz.hisname.fireflyiii.ui.transaction.TransactionSeparatorAdapter
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.openFile
import java.util.ArrayList

class AccountDetailFragment: BaseDetailFragment() {

    private val accountId: Long by lazy { arguments?.getLong("accountId") as Long  }
    private val accountType  by lazy { arguments?.getString("accountType")  }
    private val transactionAdapter by lazy { TransactionSeparatorAdapter{ data -> itemClicked(data) } }
    private val accountDetailViewModel by lazy { getImprovedViewModel(AccountDetailViewModel::class.java) }
    private var attachmentDataAdapter = arrayListOf<AttachmentData>()
    private val coloring = arrayListOf<Int>()
    private var fragmentAccountDetailBinding: FragmentAccountDetailBinding? = null
    private var detailsCardBinding: DetailsCardBinding? = null
    private val binding get() = fragmentAccountDetailBinding!!
    private val detailBinding get() = detailsCardBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentAccountDetailBinding = FragmentAccountDetailBinding.inflate(inflater, container, false)
        detailsCardBinding = binding.accountInfoCard
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        for (col in ColorTemplate.COLORFUL_COLORS) {
            coloring.add(col)
        }
        for (col in ColorTemplate.JOYFUL_COLORS){
            coloring.add(col)
        }
        accountDetailViewModel.getAccountById(accountId).observe(viewLifecycleOwner){ accountData ->
            setAccountData()
            setExpensesByCategory()
            setExpensesByBudget()
            setIncomeByCategory()
            getAccountTransaction()
        }
        accountDetailViewModel.isLoading.observe(viewLifecycleOwner){ isLoading ->
            if(isLoading == true){
                ProgressBar.animateView(binding.progressLayout.progressOverlay, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(binding.progressLayout.progressOverlay, View.GONE, 0f, 200)
            }
        }

        setDarkMode()
    }

    private fun setAccountData(){
        accountDetailViewModel.accountData.observe(viewLifecycleOwner){ list ->
            detailBinding.detailsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            detailBinding.detailsRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            detailBinding.detailsRecyclerView.adapter = BaseDetailRecyclerAdapter(list){ }
            downloadAttachment()
        }
        accountDetailViewModel.notes.observe(viewLifecycleOwner){ notes ->
            if(notes.isEmpty()){
                binding.notesCard.isGone = true
            } else {
                binding.notesText.text = notes.toMarkDown()
            }
        }
        binding.balanceHistoryCardText.text = resources.getString(R.string.account_chart_description,
                accountDetailViewModel.accountName, DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth())
    }

    private fun downloadAttachment(){
        accountDetailViewModel.accountAttachment.observe(viewLifecycleOwner) { attachment ->
            if (attachment.isNotEmpty()) {
                attachmentDataAdapter = ArrayList(attachment)
                binding.attachmentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.attachmentRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                binding.attachmentRecyclerView.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                        true, { data: AttachmentData ->
                    setDownloadClickListener(data, attachmentDataAdapter)
                }) { another: Int -> }
            }
        }
    }

    private fun setDownloadClickListener(attachmentData: AttachmentData, attachmentAdapter: ArrayList<AttachmentData>){
        accountDetailViewModel.downloadAttachment(attachmentData).observe(viewLifecycleOwner) { downloadedFile ->
            // "Refresh" the icon. From downloading to open file
            binding.attachmentRecyclerView.adapter = AttachmentRecyclerAdapter(attachmentAdapter,
                    true, { data: AttachmentData ->
                setDownloadClickListener(data, attachmentDataAdapter)
            }){ another: Int -> }
            startActivity(requireContext().openFile(downloadedFile))
        }
    }

    private fun setExpensesByCategory(){
        accountDetailViewModel.uniqueExpensesCategoryLiveData.observe(viewLifecycleOwner){ categorySumList ->
            if(categorySumList.isEmpty()){
                binding.categoryPieChart.setNoDataText(resources.getString(R.string.no_data_to_generate_chart))
            } else {
                val pieEntryArray = arrayListOf<PieEntry>()
                categorySumList.forEach { categorySum ->
                    pieEntryArray.add(PieEntry(categorySum.first, categorySum.second, categorySum.third))
                }
                val pieDataSet = PieDataSet(pieEntryArray, "").apply {
                    colors = coloring
                    valueTextSize = 15f
                    valueFormatter = PercentFormatter(binding.categoryPieChart)
                }
                binding.categoryPieChart.description.isEnabled = false
                binding.categoryPieChart.invalidate()
                binding.categoryPieChart.data = PieData(pieDataSet)
                binding.categoryPieChart.setData {
                    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                        override fun onValueSelected(entry: Entry?, h: Highlight?) {
                            val pe = entry as PieEntry
                            val entryLabel = if(entry.label.isBlank()){
                                getString(R.string.expenses_without_category)
                            } else {
                                entry.label
                            }
                            toastInfo(entryLabel + ": " + accountDetailViewModel.currencySymbol + entry.data)
                        }

                        override fun onNothingSelected() {}

                    })
                }
            }
        }
    }

    private fun setExpensesByBudget(){
        accountDetailViewModel.uniqueBudgetLiveData.observe(viewLifecycleOwner){ budgetSumList ->
            if(budgetSumList.isEmpty()){
                binding.budgetPieChart.setNoDataText(resources.getString(R.string.no_data_to_generate_chart))
            } else {
                val pieEntryArray = arrayListOf<PieEntry>()
                budgetSumList.forEach { budgetSum ->
                    pieEntryArray.add(PieEntry(budgetSum.first, budgetSum.second, budgetSum.third))
                }
                val pieDataSet = PieDataSet(pieEntryArray, "").apply {
                    colors = coloring
                    valueTextSize = 15f
                    valueFormatter = PercentFormatter(binding.budgetPieChart)
                }
                binding.budgetPieChart.description.isEnabled = false
                binding.budgetPieChart.invalidate()
                binding.budgetPieChart.data = PieData(pieDataSet)
                binding.budgetPieChart.setData {
                    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                        override fun onValueSelected(entry: Entry?, h: Highlight?) {
                            val pe = entry as PieEntry
                            val entryLabel = if (entry.label.isBlank()) {
                                getString(R.string.expenses_without_budget)
                            } else {
                                entry.label
                            }
                            toastInfo(entryLabel + ": " + accountDetailViewModel.currencySymbol + entry.data)
                        }

                        override fun onNothingSelected() {}
                    })
                }
            }
        }
    }

    private fun setDarkMode(){
        if(isDarkMode()){
            binding.categoryPieChart.legend.textColor = getCompatColor(R.color.md_white_1000)
            binding.budgetPieChart.legend.textColor = getCompatColor(R.color.md_white_1000)
        }
    }

    private fun setIncomeByCategory(){
        accountDetailViewModel.uniqueIncomeCategoryLiveData.observe(viewLifecycleOwner) { categorySumList ->
            if (categorySumList.isEmpty()) {
                binding.incomePieChart.setNoDataText(resources.getString(R.string.no_data_to_generate_chart))
            } else {
                val pieEntryArray = arrayListOf<PieEntry>()
                categorySumList.forEach { categorySum ->
                    pieEntryArray.add(PieEntry(categorySum.first, categorySum.second, categorySum.third))
                }
                val pieDataSet = PieDataSet(pieEntryArray, "").apply {
                    colors = coloring
                    valueTextSize = 15f
                    valueFormatter = PercentFormatter(binding.incomePieChart)
                }
                binding.incomePieChart.description.isEnabled = false
                binding.incomePieChart.invalidate()
                binding.incomePieChart.data = PieData(pieDataSet)
                binding.incomePieChart.setData {
                    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                        override fun onValueSelected(entry: Entry?, h: Highlight?) {
                            val pe = entry as PieEntry
                            val entryLabel = if (entry.label.isBlank()) {
                                getString(R.string.income_without_category)
                            } else {
                                entry.label
                            }
                            toastInfo(entryLabel + ": " + accountDetailViewModel.currencySymbol + entry.data)
                        }

                        override fun onNothingSelected() {}

                    })
                }
            }
        }
    }

    private fun getAccountTransaction(){
        binding.accountTransactionList.layoutManager = LinearLayoutManager(requireContext())
        binding.accountTransactionList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.accountTransactionList.adapter = transactionAdapter
        accountDetailViewModel.getTransactionList(accountId).observe(viewLifecycleOwner){ list ->
            transactionAdapter.submitData(lifecycle, list)
        }
    }

    private fun itemClicked(data: Transactions){
        parentFragmentManager.commit {
            replace(R.id.fragment_container, TransactionDetailsFragment().apply {
                arguments = bundleOf("transactionId" to data.transaction_journal_id)
            })
            addToBackStack(null)
        }
    }

    override fun deleteItem() {
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_account_title, accountDetailViewModel.accountName))
                .setMessage(resources.getString(R.string.delete_account_message, accountDetailViewModel.accountName))
                .setPositiveButton(R.string.delete_permanently) { _, _ ->
                    accountDetailViewModel.deleteAccountById(accountId).observe(viewLifecycleOwner) { isAccountDeleted ->
                        if(isAccountDeleted){
                            parentFragmentManager.popBackStack()
                            when (accountType) {
                                "asset" -> {
                                    toastSuccess(resources.getString(R.string.asset_account_deleted, accountDetailViewModel.accountName))
                                }
                                "expense" -> {
                                    toastSuccess(resources.getString(R.string.expense_account_deleted, accountDetailViewModel.accountName))
                                }
                                "revenue" -> {
                                    toastSuccess(resources.getString(R.string.revenue_account_deleted, accountDetailViewModel.accountName))
                                }
                                else -> {
                                    toastSuccess("Account Deleted")
                                }
                            }
                        }
                    }
                }
                .setNegativeButton(android.R.string.no){dialog, _ ->
                    dialog.dismiss()
                }
                .show()
    }

    override fun editItem() {
        parentFragmentManager.commit {
            replace(R.id.bigger_fragment_container, AddAccountFragment().apply{
                arguments = bundleOf("accountType" to accountType, "accountId" to accountId)
            })
            addToBackStack(null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentAccountDetailBinding = null
        detailsCardBinding = null
    }
}