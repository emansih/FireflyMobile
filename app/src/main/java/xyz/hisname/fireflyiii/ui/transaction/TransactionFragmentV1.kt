package xyz.hisname.fireflyiii.ui.transaction

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_transaction_v1.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.DateRangeViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyViewModel
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionFragment
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.EndlessRecyclerViewScrollListener
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getViewModel

class TransactionFragmentV1: BaseTransactionFragment() {

    private val result by lazy { ActionBarDrawerToggle(requireActivity(),
            fragment_transaction_v1_root, requireActivity().findViewById(R.id.activity_toolbar),
            com.mikepenz.materialdrawer.R.string.material_drawer_open,
            com.mikepenz.materialdrawer.R.string.material_drawer_close) }
    private val layoutManager by lazy { LinearLayoutManager(requireContext()) }
    private val dateRangeVm by lazy { getViewModel(DateRangeViewModel::class.java) }
    private val currencyViewModel by lazy { getImprovedViewModel(CurrencyViewModel::class.java) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        return inflater.create(R.layout.fragment_transaction_v1, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeContainer.isRefreshing = true
        setRecyclerView()
        transactionViewModel.getTransactionList(null, null,
                transactionType,1).observe(viewLifecycleOwner) { transactionList ->
            dataAdapter.clear()
            dataAdapter.addAll(transactionList)
            rtAdapter.update(transactionList)
            rtAdapter.notifyDataSetChanged()
            loadTransaction(null, null)
            swipeContainer.isRefreshing = false
        }
        pullToRefresh()
        setDateTransaction()
        setTransactionCard()
    }

    private fun setRecyclerView(){
        recycler_view.layoutManager = layoutManager
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recycler_view.adapter = rtAdapter
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
        swipeContainer.isRefreshing = true
        displayResults()
        transactionViewModel.getTransactionList(startDate, endDate, transactionType, 1).observe(viewLifecycleOwner){ transactions ->
            dataAdapter.clear()
            dataAdapter.addAll(transactions)
            rtAdapter.update(transactions)
            rtAdapter.notifyDataSetChanged()
            swipeContainer.isRefreshing = false
        }
        scrollListener  = object : EndlessRecyclerViewScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                if(!swipeContainer.isRefreshing) {
                    swipeContainer.isRefreshing = true
                    transactionViewModel.getTransactionList(startDate, endDate, transactionType, page + 1).observe(viewLifecycleOwner) { transactionList ->
                        dataAdapter.clear()
                        dataAdapter.addAll(transactionList)
                        rtAdapter.update(transactionList)
                        rtAdapter.notifyDataSetChanged()
                        displayResults()
                        swipeContainer.isRefreshing = false
                    }
                }
            }
        }
        recycler_view.addOnScrollListener(scrollListener)
    }

    override fun setupFab() {
        fab.display {
            val addTransaction = AddTransactionFragment()
            addTransaction.arguments = bundleOf("revealX" to fab.width / 2,
                    "revealY" to fab.height / 2, "transactionType" to transactionType,
                    "SHOULD_HIDE" to true)
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, addTransaction)
                addToBackStack(null)
            }
            fab.isGone = true
            fragmentContainer.isVisible = false
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
        fab.isVisible = true
    }
}