package xyz.hisname.fireflyiii.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_dashboard_recent_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.repository.transaction.TransactionsViewModel
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel

class TransactionByBudgetDialogFragment: DialogFragment() {

    private val budgetName by lazy { arguments?.getString("budgetName") }
    private lateinit var rtAdapter: TransactionRecyclerAdapter
    private val transactionViewModel by lazy { getViewModel(TransactionsViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.create(R.layout.fragment_dashboard_recent_transaction, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recentTransactionText.text = "Recent Withdrawal"
        recentTransactionList.layoutManager = LinearLayoutManager(requireContext())
        recentTransactionList.addItemDecoration(DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL))
        transactionViewModel.getTransactionListByDateAndBudget(DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), budgetName).observe(viewLifecycleOwner) { transactionData ->
            recentTransactionList.isVisible = true
            noTransactionText.isGone = true
            rtAdapter = TransactionRecyclerAdapter(transactionData){ data -> itemClicked(data) }
            recentTransactionList.adapter = rtAdapter
            rtAdapter.apply { recentTransactionList.adapter as TransactionRecyclerAdapter }
            rtAdapter.notifyDataSetChanged()
        }
        transactionViewModel.isLoading.observe(viewLifecycleOwner) {
            if(it == true){
                transactionLoader.bringToFront()
                transactionLoader.show()
            } else {
                transactionLoader.hide()
            }
        }
    }

    private fun itemClicked(data: Transactions){
        this.dismiss()
        parentFragmentManager.commit {
            replace(R.id.fragment_container, TransactionDetailsFragment().apply {
                arguments = bundleOf("transactionJournalId" to data.transaction_journal_id)
            })
            addToBackStack(null)
        }
    }

    override fun onResume() {
        super.onResume()
        val params = dialog?.window?.attributes
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        params?.height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog?.window?.attributes = params as android.view.WindowManager.LayoutParams
    }
}