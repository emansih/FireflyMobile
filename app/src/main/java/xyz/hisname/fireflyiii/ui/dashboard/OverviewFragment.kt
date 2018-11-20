package xyz.hisname.fireflyiii.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_dashboard_overview.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.transaction.TransactionsViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.zipLiveData

class OverviewFragment: BaseFragment(){

    private val transactionViewModel by lazy { getViewModel(TransactionsViewModel::class.java) }
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
        zipLiveData(transactionViewModel.getWithdrawal(DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth()),
                transactionViewModel.getDeposit(DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth())).observe(this, Observer {
            if(it.first.isNotEmpty()){
                it.first.forEachIndexed{ _, element ->
                    withdrawSum += Math.abs(element.transactionAttributes?.amount!!.toInt())
                }
                withdrawText.text = withdrawSum.toString()
            } else {
                // no withdrawal
                withdrawSum = 0
                withdrawText.text = "0"
            }
            if(it.second.isNotEmpty()){
                it.second.forEachIndexed{ _, element ->
                    depositSum += Math.abs(element.transactionAttributes?.amount!!.toInt())
                }
                incomeDigit.text = depositSum.toString()
            } else {
                // no deposit
                depositSum = 0
                incomeDigit.text = "0"
            }
            transaction = depositSum - withdrawSum
            sumText.text = transaction.toString()
        })
    }

    private fun viewReport(){
        overviewCard.setOnClickListener{
            requireFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ReportFragment(), "report")
                    .commit()
        }
    }
}
