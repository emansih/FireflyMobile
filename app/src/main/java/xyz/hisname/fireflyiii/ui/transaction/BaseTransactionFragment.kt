package xyz.hisname.fireflyiii.ui.transaction

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.EndlessRecyclerViewScrollListener
import xyz.hisname.fireflyiii.util.extension.bindView
import xyz.hisname.fireflyiii.util.extension.toastInfo

abstract class BaseTransactionFragment: BaseFragment() {

    protected var dataAdapter = arrayListOf<Transactions>()
    protected val rtAdapter by lazy { TransactionRecyclerAdapter(dataAdapter){ data -> itemClicked(data) } }
    protected val transactionType: String by lazy { arguments?.getString("transactionType") ?: "" }
    protected val noTransactionText by bindView<TextView>(R.id.listText)
    protected val noTransactionImage by bindView<ImageView>(R.id.listImage)
    protected lateinit var scrollListener: EndlessRecyclerViewScrollListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionViewModel.apiResponse.observe(this) {
            toastInfo(it)
        }
        setupFab()
    }

    abstract fun setupFab()
    abstract fun itemClicked(data: Transactions)

    protected fun displayResults(){
        swipeContainer.isRefreshing = false
        if(dataAdapter.isEmpty()){
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
        }
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().globalFAB.isGone = true
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