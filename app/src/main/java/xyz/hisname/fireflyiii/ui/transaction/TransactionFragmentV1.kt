package xyz.hisname.fireflyiii.ui.transaction

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import kotlinx.android.synthetic.main.base_swipe_layout.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.DateRangeViewModel
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionFragment
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getViewModel

class TransactionFragmentV1: BaseTransactionFragment() {

    private lateinit var result: Drawer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        return inflater.create(R.layout.fragment_transaction_v1, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeContainer.isRefreshing = true
        transactionViewModel.getTransactionList(DateTimeUtil.getTodayDate(), DateTimeUtil.getStartOfMonth(6),
                transactionType).observe(this) {
            loadTransaction(DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth())
        }
        setDateTransaction()
        setTransactionCard()
    }

    private fun setTransactionCard(){
        result = DrawerBuilder()
                .withActivity(requireActivity())
                .withDrawerGravity(Gravity.END)
                .withDisplayBelowStatusBar(false)
                .withActionBarDrawerToggle(false)
                .withCustomView(View.inflate(requireContext(), R.layout.transaction_card_layout, null))
                .buildForFragment()
        val cardRecyclerView = requireActivity().findViewById<RecyclerView>(R.id.transaction_card_recyclerview)
        runLayoutAnimation(cardRecyclerView)
        currencyViewModel.getDefaultCurrency().observe(this) { currencyData ->
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
                            currencyName, transactionType, currencySymbol)).observe(this) { transactionData ->
                val transactionArray = arrayListOf(transactionData.first, transactionData.second, transactionData.third,
                        transactionData.fourth, transactionData.fifth, transactionData.sixth)
                cardRecyclerView.adapter = TransactionMonthRecyclerView(transactionArray){
                    data: Int -> cardClicked(data)
                }.apply {
                    update(transactionArray)
                }

            }
        }
    }

    private fun cardClicked(clicky: Int){
        result.closeDrawer()
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

    private fun loadTransaction(startDate: String, endDate: String) {
        dataAdapter.clear()
        transactionViewModel.getTransactionList(startDate, endDate, transactionType).observe(this) {
            dataAdapter = ArrayList(it)
            displayResults()
        }
    }

    override fun setupFab() {
        fab.display {
            val addTransaction = AddTransactionFragment()
            addTransaction.arguments = bundleOf("revealX" to fab.width / 2,
                    "revealY" to fab.height / 2, "transactionType" to transactionType,
                    "SHOULD_HIDE" to true)
            requireFragmentManager().commit {
                replace(R.id.bigger_fragment_container, addTransaction)
                addToBackStack(null)
            }
            fab.isGone = true
            fragmentContainer.isVisible = false
        }
    }

    private fun setDateTransaction(){
        val dateRangeVm = getViewModel(DateRangeViewModel::class.java)
        zipLiveData(dateRangeVm.startDate, dateRangeVm.endDate).observe(this) { dates ->
            if(dates.first.isNotBlank() && dates.second.isNotBlank()){
                loadTransaction(dates.first, dates.second)
            }
        }
    }

    override fun itemClicked(data: Transactions) {
        requireFragmentManager().commit {
            replace(R.id.fragment_container, TransactionDetailsFragment().apply {
                arguments = bundleOf("transactionJournalId" to data.transaction_journal_id)
            })
            addToBackStack(null)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.menu_item_filter).icon = IconicsDrawable(requireContext())
                .icon(FontAwesome.Icon.faw_filter)
                .sizeDp(24)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.filter_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        R.id.menu_item_filter -> consume {
            val bottomSheetFragment = TransactionDateRangeBottomSheet()
            bottomSheetFragment.show(requireFragmentManager(), "daterangefrag" )
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        result.drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun onResume() {
        super.onResume()
        fab.isVisible = true
    }
}