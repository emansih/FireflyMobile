package xyz.hisname.fireflyiii.ui.transaction

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_dashboard_recent_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionApiResponse
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.TransactionViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.consume
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import java.util.*

class TransactionFragment: BaseFragment() {

    private val model: TransactionViewModel by lazy { getViewModel(TransactionViewModel::class.java) }
    private var dataAdapter = ArrayList<TransactionData>()
    private lateinit var rtAdapter: TransactionRecyclerAdapter
    private val transactionType: String by lazy { arguments?.getString("transactionType") ?: "" }
    private val startDate: String? by lazy { arguments?.getString("startDate") }
    private val endDate: String? by lazy { arguments?.getString("endDate")  }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        if(!Objects.equals(transactionType, "all")) {
            setHasOptionsMenu(true)
        }
        return inflater.create(R.layout.fragment_dashboard_recent_transaction, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!Objects.equals(transactionType, "all")){
            val toolbar = requireActivity().findViewById<Toolbar>(R.id.activity_toolbar)
            toolbar.overflowIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_filter)
            recentTransactionText.isVisible = false
            linedivider.isVisible = false
        }
        loadTransaction(startDate, endDate)
    }

    private fun loadTransaction(startDate: String?, endDate: String?){
        dataAdapter.clear()
        transactionLoader.bringToFront()
        transactionLoader.show()
        recentTransactionList.layoutManager = LinearLayoutManager(requireContext())
        recentTransactionList.addItemDecoration(DividerItemDecoration(recentTransactionList.context,
                DividerItemDecoration.VERTICAL))
        model.getTransactions(baseUrl,accessToken, startDate, endDate,transactionType).observe(this, Observer {
            if(it.getError() == null) {
                dataAdapter = ArrayList(it.getTransaction()?.data)
                transactionLoader.hide()
                if (dataAdapter.size == 0) {
                    recentTransactionList.isGone = true
                    noTransactionText.isVisible = true
                } else {
                    processData(transactionType, it)
                }
            }
        })
    }

    private fun processData(transactionType: String, response: TransactionApiResponse){
        if(Objects.equals(transactionType, "all")) {
            if (dataAdapter.size <= 5) {
                recentTransactionList.isVisible = true
                noTransactionText.isGone = true
                rtAdapter = TransactionRecyclerAdapter(response.getTransaction()?.data!!.toMutableList())
                recentTransactionList.adapter = rtAdapter
                rtAdapter.apply {
                    recentTransactionList.adapter as TransactionRecyclerAdapter
                    update(response.getTransaction()?.data!!.toMutableList())
                }
            } else {
                recentTransactionList.isVisible = true
                noTransactionText.isGone = true
                // More than 5 index in json so we get first 5 only
                dataAdapter.subList(5, dataAdapter.size).clear()
                rtAdapter = TransactionRecyclerAdapter(dataAdapter)
                recentTransactionList.adapter = rtAdapter
                rtAdapter.apply {
                    recentTransactionList.adapter as TransactionRecyclerAdapter
                    update(dataAdapter)
                }
            }
        } else {
            recentTransactionList.isVisible = true
            noTransactionText.isGone = true
            rtAdapter = TransactionRecyclerAdapter(response.getTransaction()?.data!!.toMutableList())
            recentTransactionList.adapter = rtAdapter
            rtAdapter.apply {
                recentTransactionList.adapter as TransactionRecyclerAdapter
                update(response.getTransaction()?.data!!.toMutableList())
            }
        }
        rtAdapter.notifyDataSetChanged()
        println("response: " + response.getTransaction()?.data)
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        if(!Objects.equals(transactionType, "all")){
            activity?.activity_toolbar?.title = transactionType.substring(0,1).toUpperCase() +
                    transactionType.substring(1)
        }
    }

    override fun onResume() {
        super.onResume()
        if(!Objects.equals(transactionType, "all")){
            activity?.activity_toolbar?.title = transactionType.substring(0,1).toUpperCase() +
                    transactionType.substring(1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.filter_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when(item?.itemId) {
        R.id.menu_item_week -> consume {
            loadTransaction(DateTimeUtil.getStartOfWeek(), DateTimeUtil.getEndOfWeek())
        }
        R.id.menu_item_month -> consume {
            loadTransaction(DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth())
        }
        R.id.menu_item_year -> consume {
            loadTransaction(DateTimeUtil.getStartOfYear(), DateTimeUtil.getEndOfYear())
        }
        else -> super.onOptionsItemSelected(item)
    }

}