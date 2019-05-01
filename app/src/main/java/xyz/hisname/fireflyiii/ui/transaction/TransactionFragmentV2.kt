package xyz.hisname.fireflyiii.ui.transaction

import android.os.Bundle
import android.view.*
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import kotlin.collections.ArrayList

class TransactionFragmentV2: BaseTransactionFragment(){

    private var currentDate = DateTimeUtil.getTodayDate()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_transaction, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        runLayoutAnimation(recycler_view)
        getDate()
        pullToRefresh()
    }

    private fun loadTransaction(){
        dataAdapter.clear()
        when (transactionType) {
            "Withdrawal" -> transactionViewModel.getWithdrawalList(currentDate, currentDate).observe(this, Observer{
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
            "Transfer" -> transactionViewModel.getTransferList(currentDate, currentDate).observe(this, Observer {
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
            "Deposit" -> transactionViewModel.getDepositList(currentDate, currentDate).observe(this, Observer {
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

    override fun setupFab(){
        addTransactionFab.apply {
            translationX = (6 * 56).toFloat()
            animate().apply {
                translationX(0f)
                interpolator = OvershootInterpolator(3f)
                startDelay = 300
                duration = 400
                start()
            }
            setOnClickListener {
                addTransactionFab.isClickable = false
                val addTransaction = AddTransactionFragment()
                addTransaction.arguments = bundleOf("revealX" to addTransactionFab.width / 2,
                        "revealY" to addTransactionFab.height / 2, "transactionType" to transactionType,
                        "SHOULD_HIDE" to true)
                requireFragmentManager().commit {
                    replace(R.id.bigger_fragment_container, addTransaction)
                    addToBackStack(null)
                }
                addTransactionFab.isClickable = true
                addTransactionFab.isVisible = false
                fragmentContainer.isVisible = false
            }
            setImageDrawable(IconicsDrawable(requireContext())
                    .icon(GoogleMaterial.Icon.gmd_add)
                    .color(ContextCompat.getColor(requireContext(), R.color.md_pink_200))
                    .sizeDp(16))
        }
    }

    private fun pullToRefresh(){
        swipeContainer.setOnRefreshListener {
            loadTransaction()
        }
    }

    private fun getDate(){
        loadTransaction()
        transaction_calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val correctDate = if(dayOfMonth in 1..9){
                "0$dayOfMonth"
            } else {
                dayOfMonth.toString()
            }
            val correctMonth: String = if(month in 0..8){
                "0" + ((1 + month).toString())
            } else {
                (month + 1).toString()
            }
            // the start of month is 0?! start calendar view is stupid really???!!
            currentDate = "$year-$correctMonth-$correctDate"
            loadTransaction()
        }
    }
}