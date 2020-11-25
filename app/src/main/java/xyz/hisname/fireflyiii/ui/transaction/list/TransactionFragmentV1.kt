package xyz.hisname.fireflyiii.ui.transaction.list

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_transaction_v1.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.transaction.DateRangeViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyViewModel
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.transaction.TransactionDateRangeBottomSheet
import xyz.hisname.fireflyiii.ui.transaction.TransactionMonthRecyclerView
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getViewModel

class TransactionFragmentV1: BaseTransactionFragment() {

    private val result by lazy { ActionBarDrawerToggle(requireActivity(),
            fragment_transaction_v1_root, requireActivity().findViewById(R.id.activity_toolbar),
            com.mikepenz.materialdrawer.R.string.material_drawer_open,
            com.mikepenz.materialdrawer.R.string.material_drawer_close) }
    private val dateRangeVm by lazy { getViewModel(DateRangeViewModel::class.java) }
    private val currencyViewModel by lazy { getImprovedViewModel(CurrencyViewModel::class.java) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        return inflater.create(R.layout.fragment_transaction_v1, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pullToRefresh()
        loadTransaction(null, null)
        setDateTransaction()
        setTransactionCard()
    }

    private fun setTransactionCard(){
        runLayoutAnimation(slider.recyclerView)
        currencyViewModel.getDefaultCurrency().observe(viewLifecycleOwner) { currencyData ->
            val currencyName = currencyData[0].currencyAttributes?.code ?: ""
            val currencySymbol = currencyData[0].currencyAttributes?.symbol ?: ""
            zipLiveData(transactionViewModel.getTotalTransactionAmountAndFreqByDateAndCurrency(
                    DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(),currencyName, transactionType, currencySymbol),
                    transactionViewModel.getTotalTransactionAmountAndFreqByDateAndCurrency(
                            DateTimeUtil.getStartOfMonth(1), DateTimeUtil.getEndOfMonth(1),
                            currencyName, transactionType, currencySymbol),
                    transactionViewModel.getTotalTransactionAmountAndFreqByDateAndCurrency(
                            DateTimeUtil.getStartOfMonth(2), DateTimeUtil.getEndOfMonth(2),
                            currencyName, transactionType, currencySymbol),
                    transactionViewModel.getTotalTransactionAmountAndFreqByDateAndCurrency(
                            DateTimeUtil.getStartOfMonth(3), DateTimeUtil.getEndOfMonth(3),
                            currencyName, transactionType, currencySymbol),
                    transactionViewModel.getTotalTransactionAmountAndFreqByDateAndCurrency(
                            DateTimeUtil.getStartOfMonth(4), DateTimeUtil.getEndOfMonth(4),
                            currencyName, transactionType, currencySymbol),
                    transactionViewModel.getTotalTransactionAmountAndFreqByDateAndCurrency(
                            DateTimeUtil.getStartOfMonth(5), DateTimeUtil.getEndOfMonth(5),
                            currencyName, transactionType, currencySymbol)).observe(viewLifecycleOwner) { transactionData ->
                val transactionArray = arrayListOf(transactionData.first, transactionData.second, transactionData.third,
                        transactionData.fourth, transactionData.fifth, transactionData.sixth)
                slider.recyclerView.adapter = TransactionMonthRecyclerView(transactionArray){
                    data: Int -> cardClicked(data)
                }.apply {
                    update(transactionArray)
                }

            }
        }
    }

    private fun cardClicked(clicky: Int){
        fragment_transaction_v1_root.closeDrawer(slider)
        when(clicky){
            0 -> loadTransaction(DateTimeUtil.getTodayDate(), DateTimeUtil.getEndOfMonth())
            1 -> loadTransaction(DateTimeUtil.getStartOfMonth(1), DateTimeUtil.getEndOfMonth(1))
            2 -> loadTransaction(DateTimeUtil.getStartOfMonth(2), DateTimeUtil.getEndOfMonth(2))
            3 -> loadTransaction(DateTimeUtil.getStartOfMonth(3), DateTimeUtil.getEndOfMonth(3))
            4 -> loadTransaction(DateTimeUtil.getStartOfMonth(4), DateTimeUtil.getEndOfMonth(4))
            5 -> loadTransaction(DateTimeUtil.getStartOfMonth(5), DateTimeUtil.getEndOfMonth(5))
            6 -> loadTransaction(DateTimeUtil.getStartOfMonth(6), DateTimeUtil.getEndOfMonth(6))
        }
    }

    private fun loadTransaction(startDate: String?, endDate: String?) {
        transactionVm.getTransactionList(startDate, endDate,
                transactionType).observe(viewLifecycleOwner) { pagingData ->
            transactionAdapter.submitData(lifecycle, pagingData)
        }
    }

    private fun setDateTransaction(){
        zipLiveData(dateRangeVm.startDate, dateRangeVm.endDate).observe(viewLifecycleOwner) { dates ->
            if(dates.first.isNotBlank() && dates.second.isNotBlank()){
                loadTransaction(dates.first, dates.second)
            }
        }
    }

    private fun pullToRefresh(){
        swipeContainer.setOnRefreshListener {
            loadTransaction(dateRangeVm.startDate.value, dateRangeVm.endDate.value)
        }
    }

    override fun itemClicked(data: Transactions) {
        parentFragmentManager.commit {
            replace(R.id.fragment_container, TransactionDetailsFragment().apply {
                arguments = bundleOf("transactionJournalId" to data.transaction_journal_id)
            })
            addToBackStack(null)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.menu_item_filter).icon = IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_filter
            sizeDp = 24
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.filter_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        R.id.menu_item_filter -> consume {
            val bottomSheetFragment = TransactionDateRangeBottomSheet()
            bottomSheetFragment.show(parentFragmentManager, "daterangefrag" )
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        result.onConfigurationChanged(newConfig)
    }


    override fun onResume() {
        super.onResume()
        extendedFab.isVisible = true
    }
}