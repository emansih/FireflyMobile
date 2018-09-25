package xyz.hisname.fireflyiii.ui.transaction.report

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.overview_tab_layout.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionApiResponse
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.TransactionViewModel
import xyz.hisname.fireflyiii.ui.transaction.TransactionRecyclerAdapter
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import java.util.*

class IncomeFragment: Fragment() {

    private val sharedPref: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }
    private val baseUrl: String by lazy { sharedPref.getString("fireflyUrl", "") }
    private val accessToken: String by lazy { sharedPref.getString("access_token","") }
    private val model: TransactionViewModel by lazy { getViewModel(TransactionViewModel::class.java) }
    private var dataAdapter = ArrayList<TransactionData>()
    private lateinit var rtAdapter: TransactionRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.overview_tab_layout, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIncome()
    }

    private fun getIncome(){
        transactionLoader.bringToFront()
        transactionLoader.show()
        transactionList.layoutManager = LinearLayoutManager(requireContext())
        transactionList.addItemDecoration(DividerItemDecoration(transactionList.context,
                DividerItemDecoration.VERTICAL))
        model.getTransactions(baseUrl,accessToken, DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), "deposits").observe(this, Observer {
            if(it.getError() == null){
                dataAdapter = ArrayList(it.getTransaction()?.data)
                transactionLoader.hide()
                if (dataAdapter.size == 0) {
                    transactionList.isGone = true
                    noTransactionText.isVisible = true
                } else {
                    processData(it)
                }

            }
        })
    }

    private fun processData(response: TransactionApiResponse){
        transactionList.isVisible = true
        noTransactionText.isGone = true
        rtAdapter = TransactionRecyclerAdapter(response.getTransaction()?.data!!.toMutableList())
        transactionList.adapter = rtAdapter
        rtAdapter.apply {
            transactionList.adapter as TransactionRecyclerAdapter
            update(response.getTransaction()?.data!!.toMutableList())
        }
        rtAdapter.notifyDataSetChanged()
    }


    companion object {
        fun newInstance(): IncomeFragment = IncomeFragment()
    }
}