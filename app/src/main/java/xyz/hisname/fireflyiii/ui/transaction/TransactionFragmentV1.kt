package xyz.hisname.fireflyiii.ui.transaction

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.base_swipe_layout.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.DateRangeViewModel
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
        transactionViewModel.getTransactionList(startDate, endDate, transactionType).observe(this, Observer {
            dataAdapter = ArrayList(it)
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