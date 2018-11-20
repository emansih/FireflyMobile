package xyz.hisname.fireflyiii.ui.transaction

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.repository.transaction.TransactionsViewModel
import xyz.hisname.fireflyiii.repository.viewmodel.DateViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*

class TransactionFragment: BaseFragment(){

    private val transactionViewModel by lazy { getViewModel(TransactionsViewModel::class.java) }
    private val dateViewModel by lazy { getViewModel(DateViewModel::class.java) }
    private var dataAdapter = ArrayList<TransactionData>()
    private lateinit var rtAdapter: TransactionRecyclerAdapter
    private val transactionType: String by lazy { arguments?.getString("transactionType") ?: "" }
    private val startDate: String? by lazy { arguments?.getString("startDate") }
    private val endDate: String? by lazy { arguments?.getString("endDate")  }
    private lateinit var transactionData: MutableList<TransactionData>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        return inflater.create(R.layout.fragment_transaction, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().activity_toolbar.overflowIcon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_filter)
        noTransactionText.isVisible = false
        requireActivity().globalFAB.isVisible = true
        setupFab()
        loadTransaction(startDate, endDate)
        pullToRefresh()
    }

    private fun loadTransaction(startDate: String?, endDate: String?){
        dataAdapter.clear()
        swipeContainer.isRefreshing = true
        runLayoutAnimation(recycler_view)
        when (transactionType) {
            "Withdrawal" -> transactionViewModel.getWithdrawalList(startDate, endDate).observe(this, Observer {
                transactionData = it
                if(transactionData.isEmpty()) {
                    recycler_view.isGone = true
                    noTransactionText.isVisible = true
                    noTransactionImage.isVisible = true
                    noTransactionImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_left))
                } else {
                    recycler_view.isVisible = true
                    noTransactionText.isGone = true
                    noTransactionImage.isGone = true
                    rtAdapter = TransactionRecyclerAdapter(transactionData, "no_type")
                    recycler_view.adapter = rtAdapter
                    rtAdapter.apply {
                        recycler_view.adapter as TransactionRecyclerAdapter
                        update(transactionData)
                    }
                    rtAdapter.notifyDataSetChanged()
                }
            })
            "Transfer" -> transactionViewModel.getTransferList(startDate, endDate).observe(this, Observer {
                transactionData = it
                if(transactionData.isEmpty()) {
                    recycler_view.isGone = true
                    noTransactionText.isVisible = true
                    noTransactionImage.isVisible = true
                    noTransactionImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_right))
                } else {
                    recycler_view.isVisible = true
                    noTransactionText.isGone = true
                    noTransactionImage.isGone = true
                    rtAdapter = TransactionRecyclerAdapter(transactionData, "no_type")
                    recycler_view.adapter = rtAdapter
                    rtAdapter.apply {
                        recycler_view.adapter as TransactionRecyclerAdapter
                        update(transactionData)
                    }
                    rtAdapter.notifyDataSetChanged()
                }
            })
            "Deposit" -> transactionViewModel.getDepositList(startDate, endDate).observe(this, Observer {
                transactionData = it
                if(transactionData.isEmpty()) {
                    recycler_view.isGone = true
                    noTransactionText.isVisible = true
                    noTransactionImage.isVisible = true
                    noTransactionImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_bank_transfer))
                } else {
                    recycler_view.isVisible = true
                    noTransactionText.isGone = true
                    noTransactionImage.isGone = true
                    rtAdapter = TransactionRecyclerAdapter(transactionData, "no_type")
                    recycler_view.adapter = rtAdapter
                    rtAdapter.apply {
                        recycler_view.adapter as TransactionRecyclerAdapter
                        update(transactionData)
                    }
                    rtAdapter.notifyDataSetChanged()
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
        requireActivity().globalFAB.apply {
            translationY = (6 * 56).toFloat()
            animate().translationY(0.toFloat())
                    .setInterpolator(OvershootInterpolator(1.toFloat()))
                    .setStartDelay(300)
                    .setDuration(400)
                    .start()
            setOnClickListener {
                val bundle = bundleOf("transactionType" to transactionType)
                requireFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container,
                                AddTransactionFragment().apply { arguments = bundle } ,"addTrans")
                        .addToBackStack(null)
                        .commit()
                requireActivity().globalFAB.isVisible = false
            }
        }
        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dy > 0 && requireActivity().globalFAB.isShown){
                    requireActivity().globalFAB.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    requireActivity().globalFAB.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.filter_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun pullToRefresh(){
        dataAdapter.clear()
        swipeContainer.setOnRefreshListener {
            loadTransaction(startDate, endDate)
        }
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        R.id.menu_item_filter -> consume {
            val bottomSheetFragment = DateRangeFragment()
            bottomSheetFragment.show(requireFragmentManager(), "daterangefrag" )
            zipLiveData(dateViewModel.startDate, dateViewModel.endDate).observe(this, Observer {
                loadTransaction(it.first, it.second)
            })
        }
        else -> super.onOptionsItemSelected(item)
    }
}