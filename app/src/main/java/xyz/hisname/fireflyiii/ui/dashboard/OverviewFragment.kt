package xyz.hisname.fireflyiii.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_dashboard_overview.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.repository.viewmodel.TransactionViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastError
import xyz.hisname.fireflyiii.util.extension.zipLiveData
import kotlin.collections.ArrayList

class OverviewFragment: BaseFragment() {

    private val model: TransactionViewModel by lazy { getViewModel(TransactionViewModel::class.java) }
    private var withdrawalAdapter = ArrayList<TransactionData>()
    private var depositAdapter = ArrayList<TransactionData>()
    private var depositSum = 0
    private var withdrawSum = 0
    private var transaction = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_dashboard_overview,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fullreport.text = resources.getString(R.string.view_report, DateTimeUtil.getCurrentMonth())
        loadTransaction()
        viewReport()
    }
    private fun loadTransaction(){
        val withdrawals = model.getTransactions(baseUrl, accessToken,
                DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(), "Withdrawal").databaseData
        val deposits = model.getTransactions(baseUrl, accessToken,
                DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(), "Deposit").databaseData
        zipLiveData(withdrawals!!, deposits!!).observe(this, Observer {
            withdrawalAdapter = ArrayList(it.first)
            if(it.first.isNotEmpty()){
                it.first.forEachIndexed{ _ , element ->
                    withdrawSum += Math.abs(element.transactionAttributes?.amount!!.toInt())
                }
                withdrawText.text = withdrawSum.toString()
            } else {
                // no withdrawal
                withdrawSum = 0
                withdrawText.text = "0"
            }
            depositAdapter = ArrayList(it.second)
            if(it.second.isNotEmpty()){
                it.second.forEachIndexed { _, element ->
                    depositSum += Math.abs(element.transactionAttributes?.amount!!.toInt())
                }
                incomeDigit.text = depositSum.toString()
            } else {
                depositSum = 0
                incomeDigit.text = "0"
            }
            transaction = depositSum - withdrawSum
            sumText.text = transaction.toString()
        })
    }

    private fun viewReport(){
        val bundle: Bundle by lazy { bundleOf("fireflyUrl" to baseUrl, "access_token" to accessToken,
                "expense" to withdrawSum.toString(), "income" to depositSum.toString()) }
        overviewCard.setOnClickListener{
            requireFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ReportFragment().apply { arguments = bundle }, "report")
                    .commit()
        }
    }

}
