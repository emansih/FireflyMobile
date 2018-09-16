package xyz.hisname.fireflyiii.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.coroutineScope
import kotlinx.coroutines.experimental.withContext
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.Data
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.TransactionViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import java.util.*

class DashboardFragment: BaseFragment() {

    private val model: TransactionViewModel by lazy { getViewModel(TransactionViewModel::class.java) }
    private var dataAdapter = ArrayList<Data>()
    private var depositSum: Int = 0
    private var withdrawSum: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_dashboard,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getTransactions()
        setRefreshing()
    }

    private fun getTransactions(){
        swipeContainer.isRefreshing = true
        model.getTransactions(baseUrl, accessToken, "01-08-2018", DateTimeUtil.getEndDateOfCurrentMonth(), "withdrawals")
                .observe(this, Observer { withDrawResponse ->
                    if (withDrawResponse.getError() == null) {
                        dataAdapter = ArrayList(withDrawResponse.getTransaction()?.data)
                        if (dataAdapter.size == 0) {
                            // no withdrawal
                            withdrawSum = 0
                            withdrawText.text = "0"
                        } else {
                            withDrawResponse.getTransaction()?.data?.forEachIndexed { _, element ->
                                withdrawSum += Math.abs(element.attributes.amount)
                            }
                            withdrawText.text = withdrawSum.toString()
                        }
                    } else {
                        // Error
                    }
                })
        model.getTransactions(baseUrl, accessToken, "01-08-2018", DateTimeUtil.getEndDateOfCurrentMonth(), "deposits").observe(this, Observer { depositResponse ->
            if (depositResponse.getError() == null) {
                dataAdapter = ArrayList(depositResponse.getTransaction()?.data)
                if (dataAdapter.size == 0) {
                    // no deposit
                    depositSum = 0
                    incomeDigit.text = "0"
                } else {
                    depositResponse.getTransaction()?.data?.forEachIndexed { _, element ->
                        depositSum += Math.abs(element.attributes.amount)
                    }
                    incomeDigit.text = depositSum.toString()
                }
            } else {
                // Error
            }

        })
        sumText.text = (depositSum - withdrawSum).toString()
        swipeContainer.isRefreshing = false
    }

    suspend fun calculateTransactions(){
        coroutineScope{
            val deposit = async{ }
            val withdrawal = async {  }
            withContext(Dispatchers.Main){

            }
        }

    }

    private fun setRefreshing(){
        swipeContainer.setOnRefreshListener {
            // Reset values first before doing network calls
            depositSum = 0
            withdrawSum = 0
            getTransactions()
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