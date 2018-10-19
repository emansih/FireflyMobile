package xyz.hisname.fireflyiii.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_dashboard_wallet.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.viewmodel.AccountsViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import java.util.ArrayList

class WalletFragment: BaseFragment() {

    private val model: AccountsViewModel by lazy { getViewModel(AccountsViewModel::class.java) }
    private var creditCard: Int = 0
    private var cash: Int = 0
    private var assets: Int = 0
    private var dataAdapter = ArrayList<AccountData>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_dashboard_wallet,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.getAccounts(baseUrl,accessToken).databaseData
        getCreditCard()
        getCash()
        getBankBalance()
    }

    private fun getCreditCard(){
        model.getAccountType(baseUrl,accessToken,"creditcard").observe(this, Observer {
            if(it.getError() == null){
                dataAdapter = ArrayList(it.getAccounts()?.data)
                if(dataAdapter.size == 0){
                    creditText.text = "0"
                } else {
                    it.getAccounts()?.data?.forEachIndexed { _, element ->
                        creditCard += Math.abs(element.accountAttributes?.current_balance!!.toInt())

                    }
                    creditText.text = creditCard.toString()
                }
            }
        })
    }

    private fun getCash(){
        model.getAccountType(baseUrl,accessToken,"cash").observe(this, Observer {
            if(it.getError() == null){
                dataAdapter = ArrayList(it.getAccounts()?.data)
                if(dataAdapter.size == 0){
                    cashText.text = "0"
                } else {
                    it.getAccounts()?.data?.forEachIndexed { _, element ->
                        cash += Math.abs(element.accountAttributes?.current_balance!!.toInt())

                    }
                    cashText.text = cash.toString()
                }
            }
        })

    }

    private fun getBankBalance(){
        model.getAccountType(baseUrl,accessToken,"asset").observe(this, Observer {
            if(it.getError() == null){
                dataAdapter = ArrayList(it.getAccounts()?.data)
                if(dataAdapter.size == 0){
                    assetsText.text = "0"
                } else {
                    it.getAccounts()?.data?.forEachIndexed { _, element ->
                        assets += Math.abs(element.accountAttributes?.current_balance!!.toInt())
                    }
                    assetsText.text = assets.toString()
                }
            }
        })
    }

}