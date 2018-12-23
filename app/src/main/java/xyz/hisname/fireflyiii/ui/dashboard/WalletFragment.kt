package xyz.hisname.fireflyiii.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_dashboard_wallet.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.account.AccountsViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyViewModel
import xyz.hisname.fireflyiii.ui.account.ListAccountFragment
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel

class WalletFragment: BaseFragment() {

    private val bundle: Bundle by lazy { bundleOf("accountType" to "all") }
    private val accountViewModel by lazy { getViewModel(AccountsViewModel::class.java) }
    private val currencyViewModel by lazy { getViewModel(CurrencyViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_dashboard_wallet,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayData()
        overviewCard.setOnClickListener {
            requireFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ListAccountFragment().apply { arguments = bundle }, "wallet")
                    .addToBackStack(null)
                    .commit()
        }
    }

    private fun displayData() {
        currencyViewModel.getDefaultCurrency().observe(this, Observer { defaultCurrency ->
            val currencyData = defaultCurrency[0].currencyAttributes
            accountViewModel.getTotalCashAccount(currencyData!!.code).observe(this, Observer { cash ->
                cashText.text = currencyData.symbol + " " + cash
            })
            accountViewModel.getTotalAssetAccount(currencyData.code).observe(this, Observer { asset ->
                assetsText.text = currencyData.symbol + " " + asset
            })
            accountViewModel.getTotalExpenseAccount(currencyData.code).observe(this, Observer { expense ->
                expenseText.text = currencyData.symbol + " " + expense
            })
        })


    }
}