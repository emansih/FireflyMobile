package xyz.hisname.fireflyiii.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.Data
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.TransactionViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastError
import xyz.hisname.fireflyiii.util.extension.zipLiveData
import java.util.*

class DashboardFragment: BaseFragment() {

    private val model: TransactionViewModel by lazy { getViewModel(TransactionViewModel::class.java) }
    private var dataAdapter = ArrayList<Data>()
    private var depositSum = 0
    private var withdrawSum = 0
    private var transaction = 0
    private var isThereError = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_dashboard,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadTransaction()
        setRefreshing()
    }


    private fun loadTransaction(){
        swipeContainer.isRefreshing = true
        val withdrawals = model.getTransactions(baseUrl, accessToken,
                DateTimeUtil.getStartDateOfCurrentMonth(), DateTimeUtil.getEndDateOfCurrentMonth(), "withdrawals")
        val deposits = model.getTransactions(baseUrl, accessToken,
                DateTimeUtil.getStartDateOfCurrentMonth(), DateTimeUtil.getEndDateOfCurrentMonth(), "deposits")
        zipLiveData(withdrawals, deposits).observe(this, Observer {
            if(it.first.getError() == null){
                dataAdapter = ArrayList(it.first.getTransaction()?.data)
                if (dataAdapter.size == 0) {
                    // no withdrawal
                    withdrawSum = 0
                    withdrawText.text = "0"
                }  else {
                    it.first.getTransaction()?.data?.forEachIndexed { _, element ->
                        withdrawSum += Math.abs(element.attributes.amount)
                    }
                    withdrawText.text = withdrawSum.toString()
                }
            } else {
                isThereError = true
                swipeContainer.isRefreshing = false
            }
            if(it.second.getError() == null){
                dataAdapter = ArrayList(it.second.getTransaction()?.data)
                if (dataAdapter.size == 0) {
                    // no deposit
                    depositSum = 0
                    incomeDigit.text = "0"
                }  else {
                    it.second.getTransaction()?.data?.forEachIndexed { _, element ->
                        depositSum += Math.abs(element.attributes.amount)
                    }
                    incomeDigit.text = withdrawSum.toString()
                }
            } else {
                isThereError = true
                swipeContainer.isRefreshing = false
            }
            transaction = depositSum - withdrawSum
            sumText.text = transaction.toString()
        })
        if(isThereError){
            toastError("There is an issue loading transactions")
        }
        swipeContainer.isRefreshing = false
    }


    private fun setRefreshing(){
        swipeContainer.setOnRefreshListener {
            // Reset values first before doing network calls
            depositSum = 0
            incomeDigit.text = "--.--"
            withdrawSum = 0
            withdrawText.text = "--.--"
            transaction = 0
            sumText.text = "--.--"
            loadTransaction()
        }
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)

    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = "Dashboard"
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = "Dashboard"
    }


}