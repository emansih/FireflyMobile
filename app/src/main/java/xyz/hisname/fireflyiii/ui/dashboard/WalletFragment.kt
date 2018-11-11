package xyz.hisname.fireflyiii.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import kotlinx.android.synthetic.main.fragment_dashboard_wallet.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.viewmodel.AccountsViewModel
import xyz.hisname.fireflyiii.ui.account.ListAccountFragment
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel

class WalletFragment: BaseFragment() {

    private val model: AccountsViewModel by lazy { getViewModel(AccountsViewModel::class.java) }
    private var loan: Double = 0.toDouble()
    private var cash: Double = 0.toDouble()
    private var assets: Double = 0.toDouble()
    private val bundle: Bundle by lazy { bundleOf("accountType" to "all") }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_dashboard_wallet,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.getAccounts(baseUrl,accessToken).databaseData
        displayData()
        overviewCard.setOnClickListener {
            requireFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ListAccountFragment().apply { arguments = bundle }, "wallet")
                    .addToBackStack(null)
                    .commit()
        }
    }

    private fun displayData() {
        launch(context = Dispatchers.Main) {
            val cashList = async(Dispatchers.IO) {
                model.getAccountType("Cash account")
            }.await()
            cashList?.forEachIndexed { _, element ->
                cash += element.accountAttributes?.current_balance!!
            }
            cashText.text = cash.toString()
            val assetList = async(Dispatchers.IO) {
                model.getAccountType("Asset account")
            }.await()
            assetList?.forEachIndexed { _, element ->
                assets += element.accountAttributes?.current_balance!!
            }
            assetsText.text = assets.toString()
            val loanList = async(Dispatchers.IO) {
                model.getAccountType("Loan")
            }.await()
            loanList?.forEachIndexed { _, element ->
                loan += element.accountAttributes?.current_balance!!
            }
            loanText.text = loan.toString()
        }
    }
}