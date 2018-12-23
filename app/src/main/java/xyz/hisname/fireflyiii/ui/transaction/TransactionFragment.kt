package xyz.hisname.fireflyiii.ui.transaction

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.repository.transaction.TransactionsViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import kotlin.collections.ArrayList

class TransactionFragment: BaseFragment(){

    private val transactionViewModel by lazy { getViewModel(TransactionsViewModel::class.java) }
    private var dataAdapter = arrayListOf<TransactionData>()
    private lateinit var rtAdapter: TransactionRecyclerAdapter
    private val transactionType: String by lazy { arguments?.getString("transactionType") ?: "" }
    private var currentDate = DateTimeUtil.getTodayDate()
    private val noTransactionText by lazy { requireActivity().findViewById<TextView>(R.id.listText) }
    private val noTransactionImage by lazy { requireActivity().findViewById<ImageView>(R.id.listImage) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        return inflater.create(R.layout.fragment_transaction, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        runLayoutAnimation(recycler_view)
        setupFab()
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
                    rtAdapter = TransactionRecyclerAdapter(dataAdapter, "no_type")
                    recycler_view.adapter = rtAdapter
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
                    noTransactionImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_right))
                } else {
                    recycler_view.isVisible = true
                    noTransactionText.isGone = true
                    noTransactionImage.isGone = true
                    rtAdapter = TransactionRecyclerAdapter(dataAdapter, "no_type")
                    recycler_view.adapter = rtAdapter
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
                    noTransactionImage.setImageDrawable(IconicsDrawable(requireContext())
                            .icon(FontAwesome.Icon.faw_exchange_alt).sizeDp(24))
                } else {
                    recycler_view.isVisible = true
                    noTransactionText.isGone = true
                    noTransactionImage.isGone = true
                    rtAdapter = TransactionRecyclerAdapter(dataAdapter, "no_type")
                    recycler_view.adapter = rtAdapter
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
        transactionViewModel.apiResponse.observe(this, Observer {
            toastInfo(it)
        })
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = transactionType.substring(0,1).toUpperCase() +
                transactionType.substring(1)

    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = transactionType.substring(0,1).toUpperCase() +
                transactionType.substring(1)

    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().globalFAB.isGone = true
    }

    private fun setupFab(){
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
                val addTransaction = AddTransactionDialog()
                addTransaction.arguments = bundleOf("revealX" to addTransactionFab.width / 2,
                        "revealY" to addTransactionFab.height / 2, "transactionType" to transactionType)
                addTransaction.show(requireFragmentManager().beginTransaction(), "add_transaction_dialog")
                addTransactionFab.isClickable = true
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
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)
    }

    private fun getDate(){
        loadTransaction()
        transaction_calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val correctDate = if(dayOfMonth in 1..9){
                "0$dayOfMonth"
            } else {
                dayOfMonth.toString()
            }
            // the start of month is 0?! start calendar view is stupid really???!!
            val correctMonth = month + 1
            currentDate = "$year-$correctMonth-$correctDate"
            loadTransaction()
        }
    }

}