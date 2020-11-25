package xyz.hisname.fireflyiii.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_dashboard_recent_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getImprovedViewModel

class RecentTransactionFragment: BaseFragment() {

    private lateinit var recyclerAdapter: TransactionAdapter
    private val recentViewModel by lazy { getImprovedViewModel(RecentViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_dashboard_recent_transaction, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadTransaction()
    }

    private fun loadTransaction() {
        recyclerAdapter = TransactionAdapter{ data -> itemClicked(data) }
        recentTransactionList.layoutManager = LinearLayoutManager(requireContext())
        recentTransactionList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recentTransactionList.adapter = recyclerAdapter
        transactionLoader.show()
        recentViewModel.getRecentTransaction(5).observe(viewLifecycleOwner) { pagingData ->
            transactionLoader.hide()
            if (recyclerAdapter.itemCount == 0) {
                recentTransactionList.isGone = true
                noTransactionText.isVisible = true
            } else {
                recentTransactionList.isVisible = true
                noTransactionText.isGone = true
                recyclerAdapter.submitData(lifecycle, pagingData)
            }
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

    override fun handleBack() {
    }

}