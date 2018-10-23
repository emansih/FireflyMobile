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
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.TransactionViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.consume
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import java.util.*

class TransactionFragment: BaseFragment(), DateRangeFragment.OnCompleteListener {

    private val model: TransactionViewModel by lazy { getViewModel(TransactionViewModel::class.java) }
    private var dataAdapter = ArrayList<TransactionData>()
    private lateinit var rtAdapter: TransactionRecyclerAdapter
    private val transactionType: String by lazy { arguments?.getString("transactionType") ?: "" }
    private val startDate: String? by lazy { arguments?.getString("startDate") }
    private val endDate: String? by lazy { arguments?.getString("endDate")  }

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
        model.getTransactions(baseUrl,accessToken, startDate, endDate,transactionType).observe(this, Observer {
            if(it.getError() == null) {
                dataAdapter = ArrayList(it.getTransaction()?.data)
                if (dataAdapter.size == 0) {
                    recycler_view.isGone = true
                    noTransactionText.isVisible = true
                    noTransactionImage.isVisible = true
                    when {
                        Objects.equals("expenses", transactionType) ->
                            noTransactionImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_left))
                        Objects.equals("transfers", transactionType) ->
                            noTransactionImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_bank_transfer))
                        Objects.equals("income", transactionType) ->
                            noTransactionImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_right))
                    }
                } else {
                    recycler_view.isVisible = true
                    noTransactionText.isGone = true
                    noTransactionImage.isGone = true
                    rtAdapter = TransactionRecyclerAdapter(it.getTransaction()?.data!!.toMutableList(), "no_type")
                    recycler_view.adapter = rtAdapter
                    rtAdapter.apply {
                        recycler_view.adapter as TransactionRecyclerAdapter
                        update(it.getTransaction()?.data!!.toMutableList())
                    }
                    rtAdapter.notifyDataSetChanged()
                }
                swipeContainer.isRefreshing = false
            }
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
                val bundle = bundleOf("fireflyUrl" to baseUrl,
                        "access_token" to accessToken, "transactionType" to transactionType)
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

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.filter_menu, menu)
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
            bottomSheetFragment.setDateListener(this)
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onComplete(startDate: String, endDate: String) {
        loadTransaction(startDate,endDate)
    }

}