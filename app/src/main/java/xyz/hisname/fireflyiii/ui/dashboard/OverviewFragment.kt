package xyz.hisname.fireflyiii.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_dashboard_overview.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.currency.CurrencyViewModel
import xyz.hisname.fireflyiii.repository.transaction.TransactionsViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.zipLiveData

class OverviewFragment: BaseFragment(){

    private val transactionViewModel by lazy { getViewModel(TransactionsViewModel::class.java) }
    private val currencyViewModel by lazy { getViewModel(CurrencyViewModel::class.java) }
    private var depositSum = 0
    private var withdrawSum = 0
    private var transaction = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_dashboard_overview,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadTransaction()
    }

    private fun loadTransaction(){
        currencyViewModel.getDefaultCurrency().observe(this, Observer { defaultCurrency ->
            if(defaultCurrency.isNotEmpty()) {
                val currencyData = defaultCurrency[0].currencyAttributes
                val currencyCode = currencyData?.code!!
                zipLiveData(transactionViewModel.getWithdrawalWithCurrencyCode(DateTimeUtil.getStartOfMonth(),
                        DateTimeUtil.getEndOfMonth(), currencyCode),
                        transactionViewModel.getDepositWithCurrencyCode(DateTimeUtil.getStartOfMonth(),
                                DateTimeUtil.getEndOfMonth(), currencyCode)).observe(this, Observer {
                    if (it.first.isNotEmpty()) {
                        it.first.forEachIndexed { _, element ->
                            withdrawSum += Math.abs(element.transactionAttributes?.amount!!.toInt())
                        }
                        withdrawText.text = currencyData?.symbol + " " + withdrawSum.toString()
                    } else {
                        // no withdrawal
                        withdrawSum = 0
                        withdrawText.text = currencyData?.symbol + " " + "0"
                    }
                    if (it.second.isNotEmpty()) {
                        it.second.forEachIndexed { _, element ->
                            depositSum += Math.abs(element.transactionAttributes?.amount!!.toInt())
                        }
                        incomeDigit.text = currencyData?.symbol + " " + depositSum.toString()
                    } else {
                        // no deposit
                        depositSum = 0
                        incomeDigit.text = currencyData?.symbol + " " + "0"
                    }
                    transaction = depositSum - withdrawSum
                    sumText.text = currencyData?.symbol + " " + transaction.toString()
                })
            }
        })

    }
}
