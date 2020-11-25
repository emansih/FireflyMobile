package xyz.hisname.fireflyiii.ui.transaction.list

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionFragment
import xyz.hisname.fireflyiii.util.extension.bindView
import xyz.hisname.fireflyiii.util.extension.display
import xyz.hisname.fireflyiii.util.extension.getImprovedViewModel
import xyz.hisname.fireflyiii.util.extension.toastInfo

abstract class BaseTransactionFragment: BaseFragment() {

    protected var dataAdapter = arrayListOf<Transactions>()
    protected val transactionType: String by lazy { arguments?.getString("transactionType") ?: "" }
    private val noTransactionText by bindView<TextView>(R.id.listText)
    private val noTransactionImage by bindView<ImageView>(R.id.listImage)
    protected val transactionVm by lazy { getImprovedViewModel(TransactionFragmentViewModel::class.java) }
    protected val transactionAdapter by lazy { TransactionAdapter{ data -> itemClicked(data) } }
    private lateinit var layoutManager: LinearLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutManager = LinearLayoutManager(requireContext())
        setRecyclerView()
        setupFab()
        displayResult()
    }

    abstract fun itemClicked(data: Transactions)

    private fun setRecyclerView(){
        recycler_view.layoutManager = layoutManager
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recycler_view.adapter = transactionAdapter
    }

    private fun setupFab(){
        extendedFab.display{
            val addTransaction = AddTransactionFragment()
            addTransaction.arguments = bundleOf("transactionType" to transactionType,
                    "SHOULD_HIDE" to true)
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, addTransaction)
                addToBackStack(null)
            }
            extendedFab.isVisible = false
            fragmentContainer.isVisible = false
        }
    }

    private fun displayResult(){
        transactionAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner){ loadStates ->
            swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
            if(loadStates.refresh !is LoadState.Loading){
                if(transactionAdapter.itemCount < 1){
                    recycler_view.isGone = true
                    noTransactionText.isVisible = true
                    noTransactionText.text = resources.getString(R.string.no_transaction_found, transactionType)
                    noTransactionImage.isVisible = true
                    noTransactionImage.setImageDrawable(IconicsDrawable(requireContext()).apply {
                        icon = FontAwesome.Icon.faw_exchange_alt
                        sizeDp = 24
                    })
                } else {
                    recycler_view.isVisible = true
                    noTransactionText.isGone = true
                    noTransactionImage.isGone = true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val toolBarTitle = when {
            transactionType.contains("Withdrawal") -> resources.getString(R.string.withdrawal)
            transactionType.contains("Deposit") -> resources.getString(R.string.deposit)
            transactionType.contains("Transfer") -> resources.getString(R.string.transfer)
            else -> ""
        }
        activity?.activity_toolbar?.title = toolBarTitle
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        val toolBarTitle = when {
            transactionType.contains("Withdrawal") -> resources.getString(R.string.withdrawal)
            transactionType.contains("Deposit") -> resources.getString(R.string.deposit)
            transactionType.contains("Transfer") -> resources.getString(R.string.transfer)
            else -> ""
        }
        activity?.activity_toolbar?.title = toolBarTitle
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }
}