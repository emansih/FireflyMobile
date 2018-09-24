package xyz.hisname.fireflyiii.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_dashboard_overview.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.TransactionViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastError
import xyz.hisname.fireflyiii.util.extension.zipLiveData
import java.util.ArrayList

class OverviewFragment: BaseFragment() {

    private val model: TransactionViewModel by lazy { getViewModel(TransactionViewModel::class.java) }
    private var dataAdapter = ArrayList<TransactionData>()
    private var depositSum = 0
    private var withdrawSum = 0
    private var transaction = 0
    private var isThereError = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_dashboard_overview,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadTransaction()
    }
    private fun loadTransaction(){
        val withdrawals = model.getTransactions(baseUrl, accessToken,
                DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(), "withdrawals")
        val deposits = model.getTransactions(baseUrl, accessToken,
                DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(), "deposits")
        zipLiveData(withdrawals, deposits).observe(this, Observer {
            if(it.first.getError() == null){
                dataAdapter = ArrayList(it.first.getTransaction()?.data)
                if (dataAdapter.size == 0) {
                    // no withdrawal
                    withdrawSum = 0
                    withdrawText.text = "0"
                }  else {
                    it.first.getTransaction()?.data?.forEachIndexed { _, element ->
                        withdrawSum += Math.abs(element.attributes.amount.toInt())
                    }
                    withdrawText.text = withdrawSum.toString()
                }
            } else {
                isThereError = true
            }
            if(it.second.getError() == null){
                dataAdapter = ArrayList(it.second.getTransaction()?.data)
                if (dataAdapter.size == 0) {
                    // no deposit
                    depositSum = 0
                    incomeDigit.text = "0"
                }  else {
                    it.second.getTransaction()?.data?.forEachIndexed { _, element ->
                        depositSum += Math.abs(element.attributes.amount.toInt())
                    }
                    incomeDigit.text = withdrawSum.toString()
                }
            } else {
                isThereError = true
            }
            transaction = depositSum - withdrawSum
            sumText.text = transaction.toString()
        })
        if(isThereError){
            toastError("There is an issue loading transactions")
        }
    }
}