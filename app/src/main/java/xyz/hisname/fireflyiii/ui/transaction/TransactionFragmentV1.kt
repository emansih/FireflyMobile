package xyz.hisname.fireflyiii.ui.transaction

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.base_swipe_layout.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.DateRangeViewModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionFragment
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getViewModel

class TransactionFragmentV1: BaseTransactionFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        return inflater.create(R.layout.fragment_transaction_v1, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        runLayoutAnimation(recycler_view, true)
        loadTransaction(null, null)
        setDateTransaction()
    }

    private fun loadTransaction(startDate: String?, endDate: String?) {
        dataAdapter.clear()
        when (transactionType) {
            "Withdrawal" -> transactionViewModel.getWithdrawalList(startDate, endDate).observe(this, Observer{
                dataAdapter = ArrayList(it)
                if(dataAdapter.isEmpty()) {
                    recycler_view.isGone = true
                    noTransactionText.isVisible = true
                    noTransactionText.text = resources.getString(R.string.no_transaction_found, transactionType)
                    noTransactionImage.isVisible = true
                    noTransactionImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_left))
                } else {
                    recycler_view.isVisible = true
                    noTransactionText.isGone = true
                    noTransactionImage.isGone = true
                    rtAdapter = TransactionRecyclerAdapter(dataAdapter){ data: TransactionData -> itemClicked(data) }
                    recycler_view.adapter = rtAdapter
                    recycler_view.addItemDecoration(DividerItemDecoration(requireContext(),
                            DividerItemDecoration.VERTICAL))
                    rtAdapter.apply {
                        recycler_view.adapter as TransactionRecyclerAdapter
                        update(dataAdapter)
                    }
                }
            })
            "Transfer" -> transactionViewModel.getTransferList(startDate, endDate).observe(this, Observer {
                dataAdapter = ArrayList(it)
                if(it.isEmpty()) {
                    recycler_view.isGone = true
                    noTransactionText.isVisible = true
                    noTransactionText.text = resources.getString(R.string.no_transaction_found, transactionType)
                    noTransactionImage.isVisible = true
                    noTransactionImage.setImageDrawable(IconicsDrawable(requireContext())
                            .icon(FontAwesome.Icon.faw_exchange_alt).sizeDp(24))
                } else {
                    recycler_view.isVisible = true
                    noTransactionText.isGone = true
                    noTransactionImage.isGone = true
                    rtAdapter = TransactionRecyclerAdapter(dataAdapter){ data: TransactionData -> itemClicked(data) }
                    recycler_view.adapter = rtAdapter
                    recycler_view.addItemDecoration(DividerItemDecoration(requireContext(),
                            DividerItemDecoration.VERTICAL))
                    rtAdapter.apply {
                        recycler_view.adapter as TransactionRecyclerAdapter
                        update(dataAdapter)
                    }
                }
            })
            "Deposit" -> transactionViewModel.getDepositList(startDate, endDate).observe(this, Observer {
                dataAdapter = ArrayList(it)
                if(it.isEmpty()) {
                    recycler_view.isGone = true
                    noTransactionText.isVisible = true
                    noTransactionText.text = resources.getString(R.string.no_transaction_found, transactionType)
                    noTransactionImage.isVisible = true
                    noTransactionImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_right))
                } else {
                    recycler_view.isVisible = true
                    noTransactionText.isGone = true
                    noTransactionImage.isGone = true
                    rtAdapter = TransactionRecyclerAdapter(dataAdapter){ data: TransactionData -> itemClicked(data) }
                    recycler_view.adapter = rtAdapter
                    recycler_view.addItemDecoration(DividerItemDecoration(requireContext(),
                            DividerItemDecoration.VERTICAL))
                    rtAdapter.apply {
                        recycler_view.adapter as TransactionRecyclerAdapter
                        update(dataAdapter)
                    }
                }
            })
        }
        transactionViewModel.isLoading.observe(this, Observer {
            swipeContainer.isRefreshing = it == true
        })

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
        zipLiveData(dateRangeVm.startDate, dateRangeVm.endDate).observe(this, Observer { dates ->
            if(dates.first.isNotBlank() && dates.second.isNotBlank()){
                loadTransaction(dates.first, dates.second)
            }
        })
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


    override fun onResume() {
        super.onResume()
        fab.isVisible = true
    }
}